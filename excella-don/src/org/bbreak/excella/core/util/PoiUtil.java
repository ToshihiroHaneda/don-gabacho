/*************************************************************************
 *
 * Copyright 2009 by bBreak Systems.
 *
 * ExCella Core - ExcelファイルをJavaから利用するための共通基盤
 *
 * $Id: PoiUtil.java 149 2010-01-19 06:44:31Z tomo-shibata $
 * $Revision: 149 $
 *
 * This file is part of ExCella Core.
 *
 * ExCella Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * only, as published by the Free Software Foundation.
 *
 * ExCella Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License version 3 for more details
 * (a copy is included in the COPYING.LESSER file that accompanied this code).
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with ExCella Core.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>
 * for a copy of the LGPLv3 License.
 *
 ************************************************************************/
package org.bbreak.excella.core.util;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.helpers.ColumnHelper;
import org.bbreak.excella.core.exception.ExCellaException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;

/**
 * POI操作ユーティリティクラス
 * 
 * @since 1.0
 */
public final class PoiUtil {

    /**
     * コンストラクタ
     */
    private PoiUtil() {
    }
    
    /** 一時テンプレートシート名 */
    public static final String TMP_SHEET_NAME = "-@%delete%_tmpSheet";

    /**
     * セルの値の取得。 セルのタイプに応じた値を返却する。<br>
     * <br>
     * 注：セルタイプが[CELL_TYPE_ERROR]の場合<br>
     * ・xls形式 ：エラーコードを返却（HSSFErrorConstantsに定義）<br>
     * ・xlsx形式 ：Excelのエラー値を返却（ex.#DIV/0!、#N/A、#REF!・・・）
     * 
     * @param cell 対象セル
     * @return 値
     */
    public static Object getCellValue( Cell cell) {
        Object value = null;

        if ( cell != null) {
            switch ( cell.getCellType()) {
                case Cell.CELL_TYPE_BLANK:
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    value = cell.getBooleanCellValue();
                    break;
                case Cell.CELL_TYPE_ERROR:
                    value = cell.getErrorCellValue();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    // 日付の場合
                    if ( isCellDateFormatted( cell)) {
                        value = cell.getDateCellValue();
                    } else {
                        value = cell.getNumericCellValue();
                    }
                    break;
                case Cell.CELL_TYPE_STRING:
                    value = cell.getStringCellValue();
                    break;
                case Cell.CELL_TYPE_FORMULA:
                    FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                    // 式を評価
                    CellValue cellValue = evaluator.evaluate( cell);
                    int cellType = cellValue.getCellType();
                    // 表か結果の型で分岐
                    switch ( cellType) {
                        case Cell.CELL_TYPE_BLANK:
                            break;
                        case Cell.CELL_TYPE_BOOLEAN:
                            value = cell.getBooleanCellValue();
                            break;
                        case Cell.CELL_TYPE_ERROR:
                            if ( cell instanceof XSSFCell) {
                                // XSSF形式の場合は、文字列を返却
                                XSSFCell xssfCell = ( XSSFCell) cell;
                                CTCell ctCell = xssfCell.getCTCell();
                                value = ctCell.getV();
                            } else if ( cell instanceof HSSFCell) {
                                // HSSF形式の場合は、エラーコードを返却
                                value = cell.getErrorCellValue();
                            }
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            // 日付の場合
                            if ( isCellDateFormatted( cell)) {
                                value = cell.getDateCellValue();
                            } else {
                                value = cell.getNumericCellValue();
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            value = cell.getStringCellValue();
                            break;
                    }
            }
        }
        return value;
    }

    /**
     * DateUtilがLocalizeされたフォーマット(年,月,日等を含むフォーマット)に対応していないため、
     * フォーマットの""で囲まれた文字列を除去するようにして対応。
     * DateUtilが対応されたらそっちを使用する。 
     * Bug 47071として報告済み
     * 
     * @param cell 対象セル
     */
    public static boolean isCellDateFormatted( Cell cell) {
        if ( cell == null) {
            return false;
        }
        boolean bDate = false;

        double d = cell.getNumericCellValue();
        if ( DateUtil.isValidExcelDate( d)) {
            CellStyle style = cell.getCellStyle();
            if ( style == null) {
                return false;
            }
            int i = style.getDataFormat();
            String fs = style.getDataFormatString();
            if ( fs != null) {
                // And '"any"' into ''
                while ( fs.contains( "\"")) {
                    int beginIdx = fs.indexOf( "\"");
                    if ( beginIdx == -1) {
                        break;
                    }
                    int endIdx = fs.indexOf( "\"", beginIdx + 1);
                    if ( endIdx == -1) {
                        break;
                    }
                    fs = fs.replaceFirst( fs.substring( beginIdx, endIdx + 1), "");
                }
            }
            bDate = DateUtil.isADateFormat( i, fs);
        }
        return bDate;
    }

    /**
     * double型の日付からDate型の日付を取得する
     * 
     * @param excelDate double型の日付
     * @return Date型の日付
     */
    public static Date getJavaDate( double excelDate) {
        return DateUtil.getJavaDate( excelDate);
    }

    /**
     * シートから指定位置の値を取得する
     * 
     * @param sheet 対象シート
     * @param rowIndex 対象行インデックス
     * @param columnIndex 対象列インデックス
     * @return 指定位置のセルの値
     */
    public static Object getCellValue( Sheet sheet, int rowIndex, int columnIndex) {
        Object value = null;

        Row row = sheet.getRow( rowIndex);
        if ( row != null) {
            Cell cell = row.getCell( columnIndex);
            if ( cell != null) {
                value = getCellValue( cell);
            }
        }

        return value;
    }

    /**
     * 指定されたクラスに合わせて出来る限り変換した値を返す
     * 
     * @param cell 対象のセル
     * @param propertyClass 欲しいJavaのクラス
     * @return 取得した値
     */
    public static Object getCellValue( Cell cell, Class<?> propertyClass) {
        if ( cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
            // セルが空
            return null;
        }

        if ( Object.class.isAssignableFrom( propertyClass)) {
            if ( Number.class.isAssignableFrom( propertyClass)) {
                Number number = ( Number) cell.getNumericCellValue();
                // 数値

                if ( propertyClass.equals( Short.class)) {
                    return number.shortValue();
                } else if ( propertyClass.equals( Integer.class)) {
                    return number.intValue();
                } else if ( propertyClass.equals( Long.class)) {
                    return number.longValue();
                } else if ( propertyClass.equals( Float.class)) {
                    return number.floatValue();
                } else if ( propertyClass.equals( Double.class)) {
                    return number.doubleValue();
                } else if ( propertyClass.equals( BigDecimal.class)) {
                    return new BigDecimal( number.doubleValue());
                } else if ( propertyClass.equals( Byte.class)) {
                    return new Byte( number.byteValue());
                } else {
                    return number;
                }
            } else if ( Date.class.isAssignableFrom( propertyClass)) {
                // 日付
                return cell.getDateCellValue();
            } else if ( String.class.isAssignableFrom( propertyClass)) {
                // 文字列
                Object value = getCellValue( cell);
                if ( value == null) {
                    return null;
                }
                String strValue = null;
                if ( value instanceof String) {
                    strValue = ( String) value;
                }
                if ( value instanceof Double) {
                    // Double -> Stringに変換する場合は整数に変換
                    strValue = String.valueOf( (( Double) value).intValue());
                } else {
                    strValue = value.toString();
                }
                return strValue;
            } else if ( Boolean.class.isAssignableFrom( propertyClass) || boolean.class.isAssignableFrom( propertyClass)) {
                // Boolean
                Object value = getCellValue( cell);
                if ( value == null) {
                    return null;
                }
                if ( value instanceof String) {
                    return Boolean.valueOf( ( String) value);
                }
                return value;
            }
        } else {
            // プリミティブ
            Object value = getCellValue( cell);
            if ( value == null) {
                return null;
            }
            if ( value instanceof Double) {
                if ( byte.class.isAssignableFrom( propertyClass)) {
                    int intValue = Double.valueOf( ( Double) value).intValue();
                    value = Byte.valueOf( String.valueOf( intValue));
                } else if ( short.class.isAssignableFrom( propertyClass)) {
                    value = Double.valueOf( ( Double) value).shortValue();
                } else if ( int.class.isAssignableFrom( propertyClass)) {
                    value = Double.valueOf( ( Double) value).intValue();
                } else if ( long.class.isAssignableFrom( propertyClass)) {
                    value = Double.valueOf( ( Double) value).longValue();
                } else if ( float.class.isAssignableFrom( propertyClass)) {
                    value = Double.valueOf( ( Double) value).floatValue();
                } else if ( double.class.isAssignableFrom( propertyClass)) {
                    value = Double.valueOf( ( Double) value).doubleValue();
                }
            }
            return value;
        }
        return null;
    }

    /**
     * セルを含むシート名の取得
     * 
     * @param cell 対象セル
     * @return シート名
     */
    public static String getSheetName( Cell cell) {
        Sheet sheet = cell.getSheet();
        return getSheetName( sheet);
    }

    /**
     * シート名の取得
     * 
     * @param sheet 対象シート
     * @return シート名
     */
    public static String getSheetName( Sheet sheet) {
        Workbook workbook = sheet.getWorkbook();
        int sheetIndex = workbook.getSheetIndex( sheet);
        return workbook.getSheetName( sheetIndex);
    }

    /**
     * ワークブックの書き込み処理
     * don-gabacho 引数をoutputStreamに変更
     * @param workbook 対象ワークブック
     * @param outputStream 対象OutputStreamStream
     */
    public static void writeBook(Workbook workbook,OutputStream outputStream) {
        // 出力
        try {
            workbook.write(outputStream);
        } catch ( IOException e) {
            throw new ExCellaException("ワークブック出力時の例外",e);
        }
    }

    /**
     * セルをコピーする。
     * 
     * @param fromCell コピー元セル
     * @param toCell コピー先セル
     */
    public static void copyCell( Cell fromCell, Cell toCell) {

        if ( fromCell != null) {

            // 値
            int cellType = fromCell.getCellType();
            switch ( cellType) {
                case Cell.CELL_TYPE_BLANK:
                    break;
                case Cell.CELL_TYPE_FORMULA:
                    String cellFormula = fromCell.getCellFormula();
                    toCell.setCellFormula( cellFormula);
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    toCell.setCellValue( fromCell.getBooleanCellValue());
                    break;
                case Cell.CELL_TYPE_ERROR:
                    toCell.setCellErrorValue( fromCell.getErrorCellValue());
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    toCell.setCellValue( fromCell.getNumericCellValue());
                    break;
                case Cell.CELL_TYPE_STRING:
                    toCell.setCellValue( fromCell.getRichStringCellValue());
                    break;
                default:
            }

            // スタイル
            if ( fromCell.getCellStyle() != null) {
                toCell.setCellStyle( fromCell.getCellStyle());
            }

            // コメント
            if ( fromCell.getCellComment() != null) {
                toCell.setCellComment( fromCell.getCellComment());
            }
        }
    }

    /**
     * 範囲をコピーする。
     * 
     * @param fromSheet コピー元シート
     * @param rangeAddress コピー元範囲
     * @param toSheet コピー先シート
     * @param toRowNum コピー先行座標
     * @param toColumnNum コピー先列座標
     * @param clearFromRange コピー元範囲クリア有無
     */
    public static void copyRange( Sheet fromSheet, CellRangeAddress rangeAddress, Sheet toSheet, int toRowNum, int toColumnNum, boolean clearFromRange) {

        if ( fromSheet == null || rangeAddress == null || toSheet == null) {
            return;
        }

        int fromRowIndex = rangeAddress.getFirstRow();
        int fromColumnIndex = rangeAddress.getFirstColumn();

        int rowNumOffset = toRowNum - fromRowIndex;
        int columnNumOffset = toColumnNum - fromColumnIndex;

        // コピー先
        CellRangeAddress toAddress = new CellRangeAddress( rangeAddress.getFirstRow() + rowNumOffset, rangeAddress.getLastRow() + rowNumOffset, rangeAddress.getFirstColumn() + columnNumOffset,
            rangeAddress.getLastColumn() + columnNumOffset);

        Workbook fromWorkbook = fromSheet.getWorkbook();
        Sheet baseSheet = fromSheet;

        Sheet tmpSheet = null;
        // コピー先が重なる場合、一時シートを利用
        if ( fromSheet.equals( toSheet) && crossRangeAddress( rangeAddress, toAddress)) {
            // 一時シートを作成
            tmpSheet = fromWorkbook.getSheet( TMP_SHEET_NAME);
            if ( tmpSheet == null) {
                tmpSheet = fromWorkbook.createSheet( TMP_SHEET_NAME);
            }
            baseSheet = tmpSheet;
            
            int lastColNum = getLastColNum(fromSheet);
            for(int i = 0; i < lastColNum; i++){
            	tmpSheet.setColumnWidth(i, fromSheet.getColumnWidth(i));
            }

            copyRange( fromSheet, rangeAddress, tmpSheet, rangeAddress.getFirstRow(), rangeAddress.getFirstColumn(), false);

            // 元をクリアする
            if ( clearFromRange) {
                clearRange( fromSheet, rangeAddress);
            }
        }

        // 結合セルの取得
        Set<CellRangeAddress> targetCellSet = getMergedAddress( baseSheet, rangeAddress);
        // コピー先の結合セルクリア
        clearRange( toSheet, toAddress);

        // 結合セルのコピー
        for ( CellRangeAddress mergeAddress : targetCellSet) {

            toSheet.addMergedRegion( new CellRangeAddress( mergeAddress.getFirstRow() + rowNumOffset, mergeAddress.getLastRow() + rowNumOffset, mergeAddress.getFirstColumn() + columnNumOffset,
                mergeAddress.getLastColumn() + columnNumOffset));

        }

        for ( int i = rangeAddress.getFirstRow(); i <= rangeAddress.getLastRow(); i++) {
            // 行
            Row fromRow = baseSheet.getRow( i);
            if ( fromRow == null) {
                continue;
            }
            Row row = toSheet.getRow( i + rowNumOffset);
            if ( row == null) {
                row = toSheet.createRow( i + rowNumOffset);
                row.setHeight( ( short) 0);
            }

            // 元より大きい場合のみ行幅コピー
            int fromRowHeight = fromRow.getHeight();
            int toRowHeight = row.getHeight();
            if ( toRowHeight < fromRowHeight) {
                row.setHeight( fromRow.getHeight());
            }

            ColumnHelper columnHelper = null;
            if ( toSheet instanceof XSSFSheet) {
                XSSFSheet xssfSheet = ( XSSFSheet) toSheet.getWorkbook().getSheetAt( toSheet.getWorkbook().getSheetIndex( toSheet));
                CTWorksheet ctWorksheet = xssfSheet.getCTWorksheet();
                columnHelper = new ColumnHelper( ctWorksheet);
             }

            for ( int j = rangeAddress.getFirstColumn(); j <= rangeAddress.getLastColumn(); j++) {
                Cell fromCell = fromRow.getCell( j);
                if ( fromCell == null) {
                    continue;
                }
                int maxColumn = SpreadsheetVersion.EXCEL97.getMaxColumns();
                if ( toSheet instanceof XSSFSheet) {
                	maxColumn = SpreadsheetVersion.EXCEL2007.getMaxColumns();
                }
                if(j + columnNumOffset >= maxColumn){
                	break;
                }
                Cell cell = row.getCell( j + columnNumOffset);
                if ( cell == null) {
                    cell = row.createCell( j + columnNumOffset);
                    if ( toSheet instanceof XSSFSheet) {
                        // XSSFの場合、設定されてない場合はコピー元の幅をセット
                        CTCol col = columnHelper.getColumn( cell.getColumnIndex(), false);
                        if ( col == null || !col.isSetWidth()) {
                            toSheet.setColumnWidth( cell.getColumnIndex(), baseSheet.getColumnWidth( j));
                        }
                    }
                }

                // セルのコピー
                copyCell( fromCell, cell);

                // 元より大きい場合のみ列幅コピー
                try {
                    int fromColumnWidth = baseSheet.getColumnWidth( j);
                    int toColumnWidth = toSheet.getColumnWidth( j + columnNumOffset);

                    if ( toColumnWidth < fromColumnWidth) {
                        toSheet.setColumnWidth( j + columnNumOffset, baseSheet.getColumnWidth( j));
                    }
                } catch ( IndexOutOfBoundsException e) {
                    // TODO 不具合解消され次第修正
                    // XSSF場合すべてデフォルト幅でcloneSheetすると、getColumnWidthでIndexOutOfBoundsExceptionが発生してしまう不具合対応
                    continue;
                }
            }
        }

        if ( tmpSheet != null) {
            // 一時シート削除
            if ( !(fromWorkbook instanceof XSSFWorkbook)) {
                // TODO 不具合解消され次第修正
                // XSSFの場合、不具合のため一時シートは削除しない
                fromWorkbook.removeSheetAt( fromWorkbook.getSheetIndex( tmpSheet));
            }
        } else if ( clearFromRange) {
            // 一時シート未使用の場合、元をクリアする
            clearRange( fromSheet, rangeAddress);
        }

    }

    /**
     * 空白範囲を挿入（下方向にシフト）する。
     * 
     * @param sheet 対象シート
     * @param rangeAddress 挿入範囲
     */
    public static void insertRangeDown( Sheet sheet, CellRangeAddress rangeAddress) {
        // 最終列の取得
        int rangeLastRowNum = getLastRowNum( sheet, rangeAddress.getFirstColumn(), rangeAddress.getLastColumn());

        // コピー範囲
        if ( rangeLastRowNum != -1 && rangeAddress.getFirstRow() <= rangeLastRowNum) {
            CellRangeAddress fromAddress = new CellRangeAddress( rangeAddress.getFirstRow(), rangeLastRowNum, rangeAddress.getFirstColumn(), rangeAddress.getLastColumn());

            copyRange( sheet, fromAddress, sheet, rangeAddress.getLastRow() + 1, rangeAddress.getFirstColumn(), true);

        }

    }

    /**
     * 列範囲における最終行番号を取得する。
     * 
     * @param sheet 対象シート
     * @param firstColumnIndex 開始列
     * @param lastColmunIndex 終了列
     * @return 最終行番号
     */
    public static int getLastRowNum( Sheet sheet, int firstColumnIndex, int lastColmunIndex) {
        // 最終行の取得
        int sheetLastRowNum = sheet.getLastRowNum();

        int rangeLastRowNum = -1;
        // 指定列範囲の最終行の取得
        for ( int i = sheetLastRowNum; 0 <= i; i--) {
            Row row = sheet.getRow( i);
            if ( row == null) {
                continue;
            }
            Iterator<Cell> rowIterator = row.iterator();
            while ( rowIterator.hasNext()) {
                Cell cell = rowIterator.next();
                if ( cell != null) {
                    if ( firstColumnIndex <= cell.getColumnIndex() && cell.getColumnIndex() <= lastColmunIndex) {
                        rangeLastRowNum = i;
                        break;
                    }
                }
            }
            if ( rangeLastRowNum != -1) {
                break;
            }
        }
        return rangeLastRowNum;
    }

    /**
     * 空白範囲を挿入（右方向にシフト）する。
     * 
     * @param sheet 対象シート
     * @param rangeAddress 挿入範囲
     */
    public static void insertRangeRight( Sheet sheet, CellRangeAddress rangeAddress) {

        int rangeLastColumn = getLastColumnNum( sheet, rangeAddress.getFirstRow(), rangeAddress.getLastRow());

        // コピー範囲
        if ( rangeLastColumn != -1 && rangeAddress.getFirstColumn() <= rangeLastColumn) {
            CellRangeAddress fromAddress = new CellRangeAddress( rangeAddress.getFirstRow(), rangeAddress.getLastRow(), rangeAddress.getFirstColumn(), rangeLastColumn);

            copyRange( sheet, fromAddress, sheet, rangeAddress.getFirstRow(), rangeAddress.getLastColumn() + 1, true);

        }
    }

    /**
     * 行範囲における最終列番号を取得する。
     * 
     * @param sheet 対象シート
     * @param firstRowIndex 開始行
     * @param lastRowIndex 終了行
     * @return 最終列番号
     */
    public static int getLastColumnNum( Sheet sheet, int firstRowIndex, int lastRowIndex) {
        // 最終列の取得
        int rangeLastColumn = -1;
        for ( int i = firstRowIndex; i <= lastRowIndex; i++) {
            Row row = sheet.getRow( i);
            if ( row == null) {
                continue;
            }
            Iterator<Cell> rowIterator = row.iterator();
            while ( rowIterator.hasNext()) {
                Cell cell = rowIterator.next();
                if ( cell != null) {
                    if ( rangeLastColumn < cell.getColumnIndex()) {
                        rangeLastColumn = cell.getColumnIndex();
                    }
                }
            }
        }
        return rangeLastColumn;
    }

    public static void deleteRangeUp( Sheet sheet, CellRangeAddress rangeAddress) {

        int rangeLastRowNum = getLastRowNum( sheet, rangeAddress.getFirstColumn(), rangeAddress.getLastColumn());

        // コピー範囲
        if ( rangeLastRowNum != -1 && rangeAddress.getFirstRow() <= rangeLastRowNum) {
            CellRangeAddress fromAddress = new CellRangeAddress( rangeAddress.getLastRow() + 1, rangeLastRowNum, rangeAddress.getFirstColumn(), rangeAddress.getLastColumn());

            copyRange( sheet, fromAddress, sheet, rangeAddress.getFirstRow(), rangeAddress.getFirstColumn(), true);

        }
    }

    public static void deleteRangeLeft( Sheet sheet, CellRangeAddress rangeAddress) {

        int rangeLastColumn = getLastColumnNum( sheet, rangeAddress.getFirstRow(), rangeAddress.getLastRow());

        // コピー範囲
        if ( rangeLastColumn != -1 && rangeAddress.getFirstColumn() <= rangeLastColumn) {
            CellRangeAddress fromAddress = new CellRangeAddress( rangeAddress.getFirstRow(), rangeAddress.getLastRow(), rangeAddress.getLastColumn() + 1, rangeLastColumn);

            copyRange( sheet, fromAddress, sheet, rangeAddress.getFirstRow(), rangeAddress.getFirstColumn(), true);

        }
    }

    /**
     * 範囲内に含まれる結合セルの範囲情報を取得する。
     * 
     * @param sheet 対象シート
     * @param rangeAddress 対象範囲
     * @return 範囲内に含まれる結合セルの範囲情報群
     */
    private static Set<CellRangeAddress> getMergedAddress( Sheet sheet, CellRangeAddress rangeAddress) {
        // 範囲切れてたらエラー
        Set<CellRangeAddress> targetCellSet = new HashSet<CellRangeAddress>();
        int fromSheetMargNums = sheet.getNumMergedRegions();
        for ( int i = 0; i < fromSheetMargNums; i++) {
            CellRangeAddress mergedAddress = null;
            if ( sheet instanceof XSSFSheet) {
                mergedAddress = (( XSSFSheet) sheet).getMergedRegion( i);
            } else if ( sheet instanceof HSSFSheet) {
                mergedAddress = (( HSSFSheet) sheet).getMergedRegion( i);
            }

            // fromAddressに入ってるか
            if ( crossRangeAddress( rangeAddress, mergedAddress)) {

                if ( !containCellRangeAddress( rangeAddress, mergedAddress)) {
                    throw new IllegalArgumentException( "There are crossing merged regions in the range.");
                }
                // OK
                targetCellSet.add( mergedAddress);
            }
        }
        return targetCellSet;
    }

    /**
     * 指定範囲をクリアする。
     * 
     * @param sheet 対象シート
     * @param rangeAddress 対象範囲
     */
    public static void clearRange( Sheet sheet, CellRangeAddress rangeAddress) {

        clearMergedRegion( sheet, rangeAddress);

        clearCell( sheet, rangeAddress);

    }

    /**
     * 指定範囲のセルをクリアする。
     * 
     * @param sheet 対象シート
     * @param rangeAddress 対象範囲
     */
    public static void clearCell( Sheet sheet, CellRangeAddress rangeAddress) {
        int fromRowIndex = rangeAddress.getFirstRow();
        int fromColumnIndex = rangeAddress.getFirstColumn();

        int toRowIndex = rangeAddress.getLastRow();
        int toColumnIndex = rangeAddress.getLastColumn();

        // セルの削除、行の削除
        Set<Row> removeRowSet = new HashSet<Row>();
        Iterator<Row> rowIterator = sheet.rowIterator();
        while ( rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if ( fromRowIndex <= row.getRowNum() && row.getRowNum() <= toRowIndex) {
                Set<Cell> removeCellSet = new HashSet<Cell>();
                Iterator<Cell> cellIterator = row.cellIterator();
                while ( cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();

                    if ( fromColumnIndex <= cell.getColumnIndex() && cell.getColumnIndex() <= toColumnIndex) {
                        removeCellSet.add( cell);
                    }
                }
                for ( Cell cell : removeCellSet) {
                    row.removeCell( cell);
                }
            }
            if ( row.getLastCellNum() == -1) {
                removeRowSet.add( row);
            }
        }
        for ( Row row : removeRowSet) {
            sheet.removeRow( row);
        }
    }

    /**
     * 指定範囲の結合セルをクリアする。
     * 
     * @param sheet 対象シート
     * @param rangeAddress 対象範囲
     */
    public static void clearMergedRegion( Sheet sheet, CellRangeAddress rangeAddress) {

        // 結合セルの取得
        Set<CellRangeAddress> clearMergedCellSet = getMergedAddress( sheet, rangeAddress);

        // 結合セルの削除
        SortedSet<Integer> deleteIndexs = new TreeSet<Integer>( Collections.reverseOrder());
        int fromSheetMargNums = sheet.getNumMergedRegions();
        for ( int i = 0; i < fromSheetMargNums; i++) {

            CellRangeAddress mergedAddress = null;
            if ( sheet instanceof XSSFSheet) {
                mergedAddress = (( XSSFSheet) sheet).getMergedRegion( i);
            } else if ( sheet instanceof HSSFSheet) {
                mergedAddress = (( HSSFSheet) sheet).getMergedRegion( i);
            }

            for ( CellRangeAddress address : clearMergedCellSet) {
                if ( mergedAddress.formatAsString().equals( address.formatAsString())) {
                    // 削除対象
                    deleteIndexs.add( i);
                    break;
                }
            }

        }
        for ( Integer index : deleteIndexs) {
            sheet.removeMergedRegion( index);
        }
    }

    /**
     * シートクローンのエラー回避用の事前処理を行う。<BR>
     * CellがCELL_TYPE_BLANKのものが行内に２つ連続した場合にエラーが発生するため、空文字を設定する。
     * 
     * @see Workbook#cloneSheet(int) cloneSheet(int)
     * @param sheet シート
     * @deprecated poi-3.5-beta7-20090607.jarより不具合解消
     */
    public static void prepareCloneSheet( Sheet sheet) {

        Iterator<Row> rowIterator = sheet.rowIterator();
        while ( rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            while ( cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                if ( cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                    cell.setCellValue( "");
                }
            }
        }
    }

    /**
     * 範囲内と重なる部分があるかを取得する。
     * 
     * @param baseAddress 基準範囲
     * @param targetAddress 対象範囲
     * @return 重なる部分がある場合はtrue、それ以外はfalse
     */
    public static boolean crossRangeAddress( CellRangeAddress baseAddress, CellRangeAddress targetAddress) {

        if ( baseAddress.getFirstRow() <= targetAddress.getLastRow() && baseAddress.getLastRow() >= targetAddress.getFirstRow()) {

            if ( baseAddress.getFirstColumn() <= targetAddress.getFirstColumn() && baseAddress.getLastColumn() >= targetAddress.getFirstColumn()) {

                return true;
            }

        }
        return false;
    }

    /**
     * 範囲内に完全に含まれるかを取得する。
     * 
     * @param baseAddress 基準範囲
     * @param targetAddress 対象範囲
     * @return 完全に含まれている場合はtrue、それ以外はfalse
     */
    public static boolean containCellRangeAddress( CellRangeAddress baseAddress, CellRangeAddress targetAddress) {

        if ( baseAddress.getFirstRow() <= targetAddress.getFirstRow() && baseAddress.getLastRow() >= targetAddress.getLastRow()) {

            if ( baseAddress.getFirstColumn() <= targetAddress.getFirstColumn() && baseAddress.getLastColumn() >= targetAddress.getLastColumn()) {

                return true;
            }

        }
        return false;
    }

    /**
     * セルにハイパーリンクを設定する。
     * 
     * @param cell セル
     * @param type リンクタイプ
     * @param address ハイパーリンクアドレス
     * @see org.apache.poi.common.usermodel.Hyperlink
     */
    public static void setHyperlink( Cell cell, int type, String address) {

        Workbook wb = cell.getRow().getSheet().getWorkbook();

        CreationHelper createHelper = wb.getCreationHelper();

        Hyperlink link = createHelper.createHyperlink( type);
        if ( link instanceof HSSFHyperlink) {
            (( HSSFHyperlink) link).setTextMark( address);
        } else if ( link instanceof XSSFHyperlink) {
            (( XSSFHyperlink) link).setAddress( address);
        }

        cell.setHyperlink( link);
    }

    /**
     * セルに値を設定する。
     * 
     * @param cell セル
     * @param value 値
     */
    public static void setCellValue( Cell cell, Object value) {

        CellStyle style = cell.getCellStyle();

        if ( value != null) {
            if ( value instanceof String) {
                cell.setCellValue( ( String) value);
            } else if ( value instanceof Number) {
                Number numValue = ( Number) value;
                if ( numValue instanceof Float) {
                    Float floatValue = ( Float) numValue;
                    numValue = new Double( String.valueOf( floatValue));
                }
                cell.setCellValue( numValue.doubleValue());
            } else if ( value instanceof Date) {
                Date dateValue = ( Date) value;
                cell.setCellValue( dateValue);
            } else if ( value instanceof Boolean) {
                Boolean boolValue = ( Boolean) value;
                cell.setCellValue( boolValue);
            }
        } else {
            cell.setCellType( Cell.CELL_TYPE_BLANK);
            cell.setCellStyle( style);
        }
    }

    /**
     * エクセルシート内のデータのあるセルの 最大列のインデックスを取得する。 
     * A列を0とする。対象セルがない場合は-1を返す。
     * 
     * @param sheet シート
     * @return データのあるセルの最大列のインデックス
     */
    public static int getLastColNum( Sheet sheet) {
        int lastColNum = 0;

        for ( int i = 0; i <= sheet.getLastRowNum(); i++) {

            if ( sheet.getRow( i) == null) {
                continue;
            }
            int tmpColNum = sheet.getRow( i).getLastCellNum();
            if ( lastColNum < tmpColNum) {
                lastColNum = tmpColNum;
            }
        }

        return lastColNum - 1;
    }
}
