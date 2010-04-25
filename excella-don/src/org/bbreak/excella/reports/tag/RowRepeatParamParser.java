/*************************************************************************
 *
 * Copyright 2009 by bBreak Systems.
 *
 * ExCella Reports - Excelファイルを利用した帳票ツール
 *
 * $Id: RowRepeatParamParser.java 5 2009-06-22 07:55:44Z tomo-shibata $
 * $Revision: 5 $
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
package org.bbreak.excella.reports.tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.bbreak.excella.core.exception.ParseException;
import org.bbreak.excella.core.util.PoiUtil;
import org.bbreak.excella.core.util.TagUtil;
import org.bbreak.excella.reports.model.ParamInfo;
import org.bbreak.excella.reports.model.ParsedReportInfo;
import org.bbreak.excella.reports.processor.ReportsParserInfo;
import org.bbreak.excella.reports.util.ReportsUtil;

/**
 * シート内の行繰り返し置換文字列を変換するパーサ
 * 
 * @since 1.0
 */
public class RowRepeatParamParser extends ReportsTagParser<Object[]> {

    /**
     * ログ
     */
    private static Log log = LogFactory.getLog( RowRepeatParamParser.class);

    /**
     * デフォルトタグ
     */
    public static final String DEFAULT_TAG = "$R[]";

    /**
     * 置換変数のパラメータ
     */
    public static final String PARAM_VALUE = "";

    /**
     * 重複非表示の調整パラメータ
     */
    protected static final String PARAM_DUPLICATE = "hideDuplicate";

    /**
     * 値の挿入方法パラメータ
     */
    public static final String PARAM_ROW_SHIFT = "rowShift";

    /**
     * 改ページするデータ数
     */
    public static final String PARAM_BREAK_NUM = "breakNum";

    /**
     * 値変更での改ページ有無パラメータ
     */
    public static final String PARAM_CHANGE_BREAK = "changeBreak";

    /**
     * 繰り返し最大回数
     */
    public static final String PARAM_REPEAT_NUM = "repeatNum";

    /**
     * シートへのハイパーリンク設定有無
     */
    public static final String PARAM_SHEET_LINK = "sheetLink";

    /**
     * シート変数の変数名
     */
    public static final String PARAM_PROPERTY = "property";

    /**
     * コンストラクタ
     */
    public RowRepeatParamParser() {
        super( DEFAULT_TAG);
    }

    /**
     * コンストラクタ
     * 
     * @param tag タグ
     */
    public RowRepeatParamParser( String tag) {
        super( tag);
    }

    @Override
    public ParsedReportInfo parse( Sheet sheet, Cell tagCell, Object data) {

        Map<String, String> paramDef = TagUtil.getParams( tagCell.getStringCellValue());

        // パラメータチェック
        checkParam( paramDef, tagCell);

        String tag = tagCell.getStringCellValue();
        ReportsParserInfo reportsParserInfo = ( ReportsParserInfo) data;
        // 置換
        Object[] paramValues = null;
        try {
            // 値の挿入方法
            boolean rowShift = false;
            if ( paramDef.containsKey( PARAM_ROW_SHIFT)) {
                rowShift = Boolean.valueOf( paramDef.get( PARAM_ROW_SHIFT));
            }
            // 重複非表示
            boolean duplicate = false;
            if ( paramDef.containsKey( PARAM_DUPLICATE)) {
                duplicate = Boolean.valueOf( paramDef.get( PARAM_DUPLICATE));
            }
            // 改ページするデータ数
            Integer breakNum = null;
            if ( paramDef.containsKey( PARAM_BREAK_NUM)) {
                breakNum = Integer.valueOf( paramDef.get( PARAM_BREAK_NUM));
            }
            // 値変更改ページ有無
            boolean changeBreak = false;
            if ( paramDef.containsKey( PARAM_CHANGE_BREAK)) {
                changeBreak = Boolean.valueOf( paramDef.get( PARAM_CHANGE_BREAK));
            }
            // 繰り返し最大回数
            Integer repeatNum = null;
            if ( paramDef.containsKey( PARAM_REPEAT_NUM)) {
                repeatNum = Integer.valueOf( paramDef.get( PARAM_REPEAT_NUM));
            }
            // シートハイパーリンク設定有無
            boolean sheetLink = false;
            if ( paramDef.containsKey( PARAM_SHEET_LINK)) {
                sheetLink = Boolean.valueOf( paramDef.get( PARAM_SHEET_LINK));
            }
            // シート変数
            String propertyName = null;
            if ( paramDef.containsKey( PARAM_PROPERTY)) {
                propertyName = paramDef.get( PARAM_PROPERTY);
            }
            // 置換変数の取得
            String replaceParam = paramDef.get( PARAM_VALUE);
            // システム変数
            if ( ReportsUtil.VALUE_SHEET_NAMES.equals( replaceParam)) {
                // シート名
                paramValues = ReportsUtil.getSheetNames( reportsParserInfo.getReportBook()).toArray();
            } else if ( ReportsUtil.VALUE_SHEET_VALUES.equals( replaceParam)) {
                // シート値
                paramValues = ReportsUtil.getSheetValues( reportsParserInfo.getReportBook(), propertyName, reportsParserInfo.getReportParsers()).toArray();
            } else {
                // 置換する値を取得
                ParamInfo paramInfo = reportsParserInfo.getParamInfo();
                if ( paramInfo != null) {
                    paramValues = getParamData( paramInfo, replaceParam);
                }
            }

            if ( paramValues == null || paramValues.length == 0) {
                // 空文字置換
                paramValues = new Object[] {null};
            }

            int shiftNum = paramValues.length;
            if ( repeatNum != null && repeatNum < shiftNum) {
                shiftNum = repeatNum;
            }

            if ( shiftNum > 1) {
                // 値の数分シフト(１つであれば、シフトしない)
                if ( !rowShift) {
                    CellRangeAddress rangeAddress = new CellRangeAddress( tagCell.getRowIndex(), tagCell.getRowIndex() + shiftNum - 2, tagCell.getColumnIndex(), tagCell.getColumnIndex());
                    PoiUtil.insertRangeDown( sheet, rangeAddress);
                } else {
                    sheet.shiftRows( tagCell.getRowIndex() + 1, sheet.getLastRowNum(), shiftNum - 1);
                }
            }
            
            Workbook workbook = sheet.getWorkbook();
            String sheetName = workbook.getSheetName( workbook.getSheetIndex( sheet));
            //シート名
            List<String> sheetNames = ReportsUtil.getSheetNames( reportsParserInfo.getReportBook());
            //変換値
            List<Object> resultValues = new ArrayList<Object>();
            Object beforeValue = null;
            for ( int i = 0; i < shiftNum; i++) {
                Row row = sheet.getRow( tagCell.getRowIndex() + i);
                if ( row == null) {
                    row = sheet.createRow( tagCell.getRowIndex() + i);
                }
                Cell cell = row.getCell( tagCell.getColumnIndex());
                if ( cell == null) {
                    cell = row.createCell( tagCell.getColumnIndex());
                }
                // コピー(スタイル情報の設定)
                PoiUtil.copyCell( tagCell, cell);
                // 置換
                Object value = null;
                if ( beforeValue == null || !(duplicate && beforeValue.equals( paramValues[i]))) {
                    value = paramValues[i];
                }
                if ( log.isDebugEnabled()) {
                    log.debug( "[シート名=" + sheetName + ",セル=(" + cell.getRowIndex() + "," + cell.getColumnIndex() + ")]  " + tag + " ⇒ " + value);
                }
                PoiUtil.setCellValue( cell, value);
                resultValues.add( value);
                
                if ( sheetLink) {
                    if ( i < sheetNames.size()) {
                        PoiUtil.setHyperlink( cell, Hyperlink.LINK_DOCUMENT, "'" + sheetNames.get( i) + "'!A1");
                        if ( log.isDebugEnabled()) {
                            log.debug( "[シート名=" + sheetName + ",セル=(" + cell.getRowIndex() + "," + cell.getColumnIndex() + ")]  Hyperlink ⇒ " + "'" + sheetNames.get( i) + "'!A1");
                        }
                    }
                }

                if ( cell instanceof HSSFCell) {
                    // TODO XSSFのRowBreakは正常に動作しないため、HSSFのみ
                    if ( breakNum != null && (i + 1) % breakNum == 0) {
                        sheet.setRowBreak( row.getRowNum());
                    }
                    if ( changeBreak && beforeValue != null && beforeValue.equals( paramValues[i])) {
                        sheet.setRowBreak( row.getRowNum());
                    }
                }
                beforeValue = paramValues[i];
            }
            
            // 解析結果の生成
            ParsedReportInfo parsedReportInfo = new ParsedReportInfo();
            parsedReportInfo.setParsedObject( resultValues);
            parsedReportInfo.setDefaultRowIndex( tagCell.getRowIndex());
            parsedReportInfo.setDefaultColumnIndex( tagCell.getColumnIndex());
            parsedReportInfo.setRowIndex( tagCell.getRowIndex() + shiftNum - 1);
            parsedReportInfo.setColumnIndex( tagCell.getColumnIndex());
            if ( log.isDebugEnabled()) {
                log.debug(parsedReportInfo);
            }
            return parsedReportInfo;

        } catch ( Exception e) {
            throw new ParseException( tagCell, e);
        }
    }

    /**
     * 不正なパラメータがある場合、ParseExceptionをthrowする。
     * 
     * @param paramDef パラメータ定義
     * @param tagCell タグセル
     */
    private void checkParam( Map<String, String> paramDef, Cell tagCell) {
        // シートハイパーリンク設定有無と重複非表示は重複不可
        if ( paramDef.containsKey( PARAM_DUPLICATE) && paramDef.containsKey( PARAM_SHEET_LINK)) {
            throw new ParseException( tagCell, "二重定義：" + PARAM_DUPLICATE + "," + PARAM_SHEET_LINK);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.bbreak.excella.reports.tag.ReportsTagParser#useControlRow()
     */
    @Override
    public boolean useControlRow() {
        return false;
    }

}
