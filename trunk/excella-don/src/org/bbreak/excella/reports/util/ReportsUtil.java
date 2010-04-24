/*************************************************************************
 *
 * Copyright 2009 by bBreak Systems.
 *
 * ExCella Reports - Excelファイルを利用した帳票ツール
 *
 * $Id: ReportsUtil.java 98 2010-01-13 02:12:49Z tomo-shibata $
 * $Revision: 98 $
 *
 * This file is part of ExCella Reports.
 *
 * ExCella Reports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * only, as published by the Free Software Foundation.
 *
 * ExCella Reports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License version 3 for more details
 * (a copy is included in the COPYING.LESSER file that accompanied this code).
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with ExCella Reports .  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>
 * for a copy of the LGPLv3 License.
 *
 ************************************************************************/
package org.bbreak.excella.reports.util;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.bbreak.excella.core.exception.ParseException;
import org.bbreak.excella.core.util.PoiUtil;
import org.bbreak.excella.reports.model.ParamInfo;
import org.bbreak.excella.reports.model.ReportBook;
import org.bbreak.excella.reports.model.ReportSheet;
import org.bbreak.excella.reports.tag.ReportsTagParser;
import org.bbreak.excella.reports.tag.SingleParamParser;

/**
 * 帳票用ユーティリティクラス
 * 
 * @since 1.0
 */
public final class ReportsUtil {

    /**
     * コンストラクタ
     */
    private ReportsUtil() {
    }

    /**
     * システム変数：シート名
     */
    public static final String VALUE_SHEET_NAMES = "#SHEET_NAME[]";

    /**
     * システム変数：シート値
     */
    public static final String VALUE_SHEET_VALUES = "#SHEET_VALUE[]";

    /**
     * シート名と一致する帳票シート情報を取得する。
     * 
     * @param sheetName シート名
     * @param reportBook 帳票ワークブック情報
     * @return シート名と一致する帳票シート情報
     */
    public static ReportSheet getReportSheet( String sheetName, ReportBook reportBook) {

        if ( reportBook != null) {
            for ( ReportSheet reportSheet : reportBook.getReportSheets()) {

                if ( reportSheet != null && reportSheet.getSheetName().equals( sheetName)) {
                    return reportSheet;
                }
            }
        }

        return null;
    }

    /**
     * シート名リストを取得する。
     * 
     * @param reportBook 帳票シート情報
     * @return シート名リスト
     */
    public static List<String> getSheetNames( ReportBook reportBook) {
        List<String> names = new ArrayList<String>();

        for ( ReportSheet reportSheet : reportBook.getReportSheets()) {
            names.add( reportSheet.getSheetName());
        }

        return names;
    }

    /**
     * パラメータ情報（子パラメータを含む）より、置換変数名で指定されている変換値を取得する。<BR>
     * 
     * <pre>
     * 置換変数文字列指定方法
     * タグ名:置換変数名
     * 例）
     * $R[]:金額
     * $C:日付
     * $:名前
     * 名前（単純置換の場合は『タグ名:』は省略可…『$:』が補完される）  
     * 
     * ブロックタグ($BR[]、$BC[])は『.』で子パラメータを指定する。
     * ブロックタグが複数でも連続して指定可能
     * 　ブロックタグ名:置換変数名.ブロックタグ名:置換変数名.タグ名:置換変数名
     * 例）
     * $BR[]:支店.$R[]:担当者
     * $BR[]:支店.支店名
     * </pre>
     * 
     * @param info パラメータ情報
     * @param propertyNameString 置換変数文字列
     * @param parsers 使用中のパーサ群
     * @return 置換変数文字列に対応する変換値リスト
     */
    @SuppressWarnings("unchecked")
    public static List<Object> getParamValues( ParamInfo info, String propertyNameString, List<ReportsTagParser<?>> parsers) {

        String[] levels = propertyNameString.split( "\\.");

        List<Object> paramValues = new ArrayList<Object>();

        ParamInfo[] paramInfos = new ParamInfo[] {info};
        for ( String level : levels) {

            String tagName = null;
            String propertyName = null;
            if ( level.indexOf( ":") != -1) {
                // タグあり
                String[] values = level.split( ":");
                tagName = values[0];
                propertyName = values[1];
            } else {
                // タグなし⇒単純置換とする
                tagName = SingleParamParser.DEFAULT_TAG;
                propertyName = level;
            }

            List<SingleParamParser> singleParsers = new ArrayList<SingleParamParser>();
            for ( ReportsTagParser<?> reportsParser : parsers) {
                if ( reportsParser instanceof SingleParamParser) {
                    singleParsers.add( ( SingleParamParser) reportsParser);
                }
            }

            // どのパーサか特定する
            ReportsTagParser<?> targetParser = null;
            for ( ReportsTagParser<?> tagParser : parsers) {
                if ( tagParser.getTag().equals( tagName)) {
                    targetParser = tagParser;
                }
            }
            if ( targetParser == null) {
                // 対象パーサなし
                break;
            }

            // 値の取得
            Object object = targetParser.getParamData( paramInfos[0], propertyName);
            if ( object != null) {
                if ( object instanceof ParamInfo[]) {
                    // $BR[],$BC[]
                    List<ParamInfo> newParamInfos = new ArrayList<ParamInfo>();
                    for ( ParamInfo paramInfo : paramInfos) {
                        ParamInfo[] params = ( ParamInfo[]) targetParser.getParamData( paramInfo, propertyName);
                        if ( params != null) {
                            newParamInfos.addAll( Arrays.asList( params));
                        }
                    }
                    paramInfos = newParamInfos.toArray( new ParamInfo[newParamInfos.size()]);
                } else if ( object.getClass().isArray()) {
                    if ( Array.getLength( object) == 0) {
                        continue;
                    }

                    if (Array.get( object, 0) instanceof String 
                            || Array.get( object, 0) instanceof Number 
                            || Array.get( object, 0) instanceof Date 
                            || Array.get( object, 0) instanceof Boolean) {
                        // $R[],$C[]
                        for ( ParamInfo paramInfo : paramInfos) {
                            Object arrayObj = targetParser.getParamData( paramInfo, propertyName);
                            if ( arrayObj != null) {
                                for ( int i = 0; i < Array.getLength( arrayObj); i++) {
                                    paramValues.add( Array.get( arrayObj, i));
                                }
                            }
                        }
                    } else {
                        // $BR[],$BC[]
                        List<ParamInfo> newParamInfos = new ArrayList<ParamInfo>();
                        for ( ParamInfo paramInfo : paramInfos) {
                            Object[] params = ( Object[]) targetParser.getParamData( paramInfo, propertyName);

                            // POJOをParamInfoに変換する
                            for ( Object obj : params) {
                                if ( obj instanceof ParamInfo) {
                                    newParamInfos.add( ( ParamInfo) obj);
                                    continue;
                                }
                                ParamInfo childParamInfo = new ParamInfo();
                                
                                Map<String, Object> map = null;
                                try {
                                    map = PropertyUtils.describe( obj);
                                } catch ( Exception e) {
                                    throw new RuntimeException("不正なオブジェクトが設定されています。", e);
                                }
                                for ( Map.Entry<String, Object> entry : map.entrySet()) {
                                    for ( ReportsTagParser<?> parser : singleParsers) {
                                        childParamInfo.addParam( parser.getTag(), entry.getKey(), entry.getValue());
                                    }
                                }
                                newParamInfos.add( childParamInfo);
                            }
                        }
                        paramInfos = newParamInfos.toArray( new ParamInfo[newParamInfos.size()]);
                    }

                } else if ( object instanceof Collection<?>) {
                    for ( ParamInfo paramInfo : paramInfos) {
                        Collection<?> collection = ( Collection<?>) targetParser.getParamData( paramInfo, propertyName);
                        if ( collection != null) {
                            paramValues.addAll( collection);
                        }
                    }
                } else {
                    // $,$I
                    for ( ParamInfo paramInfo : paramInfos) {
                        Object value = targetParser.getParamData( paramInfo, propertyName);
                        if ( value != null) {
                            paramValues.add( value);
                        }
                    }

                }
            }

        }

        return paramValues;

    }

    /**
     * シートの値を取得する。
     * 
     * @param reportBook 帳票シート情報
     * @param propertyName 単純置換変数
     * @param parsers 使用中のパーサ群
     * @return 各シートの変数値
     */
    public static List<Object> getSheetValues( ReportBook reportBook, String propertyName, List<ReportsTagParser<?>> parsers) {

        List<Object> paramValues = new ArrayList<Object>();

        // 子は取得しない
        if ( propertyName.indexOf( ".") != -1) {
            return paramValues;
        }

        // 単純置換変数のみ
        if ( propertyName.indexOf( ":") != -1) {
            return paramValues;
        }

        for ( ReportSheet reportSheet : reportBook.getReportSheets()) {
            if ( parsers != null) {
                for ( ReportsTagParser<?> tagParser : parsers) {
                    if ( tagParser instanceof SingleParamParser) {
                        Object object = reportSheet.getParam( tagParser.getTag(), propertyName);
                        if ( object != null) {
                            paramValues.add( object);
                        }
                    }
                }
            }
        }

        return paramValues;

    }

    /**
     * 合計値を取得する。
     * 
     * @param info パラメータ情報
     * @param propertyName 置換変数文字列
     * @param parsers 使用中のパーサ群
     * @return 合計値
     */
    public static BigDecimal getSumValue( ParamInfo info, String propertyName, List<ReportsTagParser<?>> parsers) {
        List<?> values = ReportsUtil.getParamValues( info, propertyName, parsers);

        BigDecimal sumValue = BigDecimal.ZERO;

        for ( Object value : values) {
            if ( value instanceof Number) {

                Number num = ( Number) value;
                BigDecimal decimal = null;
                if ( value instanceof Byte) {
                    decimal = new BigDecimal( num.intValue());
                } else if ( value instanceof Short) {
                    decimal = new BigDecimal( num.intValue());
                } else if ( value instanceof Integer) {
                    decimal = new BigDecimal( num.intValue());
                } else if ( value instanceof Long) {
                    decimal = new BigDecimal( num.longValue());
                } else if ( value instanceof Float) {
                    Float floatValue = ( Float) value;
                    decimal = new BigDecimal( String.valueOf( floatValue));
                } else if ( value instanceof Double) {
                    decimal = BigDecimal.valueOf( num.doubleValue());
                } else if ( value instanceof BigInteger) {
                    decimal = new BigDecimal( ( BigInteger) value);
                } else if ( value instanceof BigDecimal) {
                    decimal = ( BigDecimal) value;
                }

                if ( decimal != null) {
                    sumValue = sumValue.add( decimal);
                }
            }
        }
        return sumValue;
    }

    /**
     * 指定された行、列座標のセルを含む結合セルを取得する。
     * 
     * @param sheet 対象シート
     * @param rowIndex 行インデックス
     * @param columnIndex 列インデックス
     * @return 結合セル
     */
    public static CellRangeAddress getMergedAddress( Sheet sheet, int rowIndex, int columnIndex) {

        CellRangeAddress rangeAddress = new CellRangeAddress( rowIndex, rowIndex, columnIndex, columnIndex);

        int fromSheetMargNums = sheet.getNumMergedRegions();
        for ( int i = 0; i < fromSheetMargNums; i++) {
            CellRangeAddress mergedAddress = null;
            if ( sheet instanceof XSSFSheet) {
                mergedAddress = (( XSSFSheet) sheet).getMergedRegion( i);
            } else if ( sheet instanceof HSSFSheet) {
                mergedAddress = (( HSSFSheet) sheet).getMergedRegion( i);
            }

            // fromAddressに入ってるか
            if ( PoiUtil.containCellRangeAddress( mergedAddress, rangeAddress)) {
                return mergedAddress;
            }
        }
        return null;
    }
    
    /**
     * オプション定義が範囲（XXXCell=n:n）から、nを数値で取得する。
     * 
     * @param cellParam 範囲指定（n:n）
     * @param tagName タグ名称（エラー時出力用）
     * @return intの配列（row、col）
     * @throws ParseException
     */
    public static int[] getCellIndex( String cellParam, String tagName) throws ParseException {
        String[] cellIndex = cellParam.split( ":");
        if ( cellIndex.length != 2) {
            throw new ParseException( "cellParam:" + cellParam);
        }
        // 数値（int型）に置換
        int rowIndex = getIndex( cellIndex[0], tagName);
        int colIndex = getIndex( cellIndex[1], tagName);
        int[] index = {rowIndex, colIndex};

        return index;
    }

    /**
     * オプション定義の範囲の数値の妥当性チェック
     * 
     * @param index 範囲指定された数字
     * @param tagName タグ名称（エラー出力用）
     * @return 指定された数字をintで返却
     * @throws ParseException
     * @exception 数値に変換できない場合
     */
    private static int getIndex( String index, String tagName) throws ParseException {
        int intIndex = 0;
        try {
            intIndex = Integer.parseInt( index);
        } catch ( Exception e) {
            throw new ParseException( "index:" + index);
        }
        return intIndex;
    }

    /**
     * 対象シートの指定された範囲のセルに書かれている文字列をString[行番号][列番号]の形式で取得する。 セルに書かれている値が文字列でない場合は、nullがセットされる。
     * 
     * @param sheet 対象となるシート
     * @param bStartRowIndex 範囲開始行番号
     * @param bEndRowIndex 範囲終了行番号
     * @param bStartColIndex 範囲開始列番号
     * @param bEndColIndex 範囲終了列番号
     * @return セル文字列
     */
    public static Object[][] getBlockCellValue( Sheet sheet, int bStartRowIndex, int bEndRowIndex, int bStartColIndex, int bEndColIndex) {
        Object[][] blockCellValue = new Object[bEndRowIndex - bStartRowIndex + 1][bEndColIndex - bStartColIndex + 1];
        int row = 0;
        for ( int bRowIndex = bStartRowIndex; bRowIndex <= bEndRowIndex; bRowIndex++) {
            int col = 0;
            for ( int bColIndex = bStartColIndex; bColIndex <= bEndColIndex; bColIndex++) {
                if ( sheet.getRow( bRowIndex) != null && sheet.getRow( bRowIndex).getCell( bColIndex) != null) {

                    blockCellValue[row][col] = PoiUtil.getCellValue( sheet.getRow( bRowIndex).getCell( bColIndex));
                } else {
                    blockCellValue[row][col] = null;
                }
                col++;
            }
            row++;
        }
        return blockCellValue;
    }

    /**
     * 対象シートの指定された範囲のセルに書かれている文字列をCellStyle[行番号][列番号]の形式で取得する。 セルの値がnullの場合、nullがセットされる。
     * 
     * @param sheet 対象となるシート
     * @param bStartRowIndex 範囲開始行番号
     * @param bEndRowIndex 範囲終了行番号
     * @param bStartColIndex 範囲開始列番号
     * @param bEndColIndex 範囲終了列番号
     * @return セルスタイル
     */
    public static CellStyle[][] getBlockCellStyle( Sheet sheet, int bStartRowIndex, int bEndRowIndex, int bStartColIndex, int bEndColIndex) {
        CellStyle[][] blockCellStyle = new CellStyle[bEndRowIndex - bStartRowIndex + 1][bEndColIndex - bStartColIndex + 1];
        int row = 0;
        for ( int bRowIndex = bStartRowIndex; bRowIndex <= bEndRowIndex; bRowIndex++) {
            int col = 0;
            for ( int bColIndex = bStartColIndex; bColIndex <= bEndColIndex; bColIndex++) {
                if ( sheet.getRow( bRowIndex) != null && sheet.getRow( bRowIndex).getCell( bColIndex) != null) {

                    blockCellStyle[row][col] = sheet.getRow( bRowIndex).getCell( bColIndex).getCellStyle();
                } else {
                    blockCellStyle[row][col] = null;
                }

                col++;
            }
            row++;
        }

        return blockCellStyle;
    }

    /**
     * 対象シートの指定された範囲のセルのタイプint[行番号][列番号]の形式で取得する。 セルの値がnullの場合、Cell.CELL_TYPE_BLANKがセットされる。
     * 
     * @param sheet 対象となるシート
     * @param bStartRowIndex 範囲開始行番号
     * @param bEndRowIndex 範囲終了行番号
     * @param bStartColIndex 範囲開始列番号
     * @param bEndColIndex 範囲終了列番号
     * @return セルのタイプ
     */
    public static int[][] getBlockCellType( Sheet sheet, int bStartRowIndex, int bEndRowIndex, int bStartColIndex, int bEndColIndex) {
        int[][] blockCellType = new int[bEndRowIndex - bStartRowIndex + 1][bEndColIndex - bStartColIndex + 1];
        int rowIdx = 0;
        for ( int bRowIndex = bStartRowIndex; bRowIndex <= bEndRowIndex; bRowIndex++) {
            int colIdx = 0;
            for ( int bColIndex = bStartColIndex; bColIndex <= bEndColIndex; bColIndex++) {
                Row row = sheet.getRow(  bRowIndex);
                if ( row != null && row.getCell( bColIndex) != null) {
                    blockCellType[rowIdx][colIdx] = row.getCell( bColIndex).getCellType();
                } else {
                    blockCellType[rowIdx][colIdx] = Cell.CELL_TYPE_BLANK;
                }

                colIdx++;
            }
            rowIdx++;
        }

        return blockCellType;
    }

    /**
     * 対象シートの指定された範囲の行の高さをfloat[行番号]の形式で取得する。 高さがnullの場合、nullがセットされる。
     * 
     * @param sheet 対象となるシート
     * @param bStartRowIndex 範囲開始行番号
     * @param bEndRowIndex 範囲終了行番号
     * @return 行の高さ
     */
    public static float[] getRowHeight(Sheet sheet, int bStartRowIndex, int bEndRowIndex){
        
        float[] height = new float[bEndRowIndex - bStartRowIndex + 1];
        int rowIdx = 0;
        for ( int bRowIndex = bStartRowIndex; bRowIndex <= bEndRowIndex; bRowIndex++) {
            Row row = sheet.getRow(  bRowIndex);
            height[rowIdx] = row.getHeightInPoints();
            rowIdx++;
        }
        return height;
        
    }

}
