/*************************************************************************
 *
 * Copyright 2009 by bBreak Systems.
 *
 * ExCella Reports - Excelファイルを利用した帳票ツール
 *
 * $Id: ColRepeatParamParser.java 5 2009-06-22 07:55:44Z tomo-shibata $
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Hyperlink;
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
 * シート内の置換文字列を横方向に繰り返し変換するパーサ
 * 
 * @since 1.0
 */
public class ColRepeatParamParser extends ReportsTagParser<Object[]> {

    /**
     * ログ
     */
    private static Log log = LogFactory.getLog( ColRepeatParamParser.class);

    /**
     * デフォルトタグ
     */
    public static final String DEFAULT_TAG = "$C[]";

    /**
     * 置換変数のパラメータ
     */
    public static final String PARAM_VALUE = "";

    /**
     * 重複非表示の調整パラメータ
     */
    protected static final String PARAM_DUPLICATE = "hideDuplicate";

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
    public ColRepeatParamParser() {
        super( DEFAULT_TAG);
    }

    /**
     * コンストラクタ
     * 
     * @param tag タグ
     */
    public ColRepeatParamParser( String tag) {
        super( tag);
    }

    @Override
    public boolean useControlRow() {
        return false;
    }

    @Override
    public ParsedReportInfo parse( Sheet sheet, Cell tagCell, Object data) {

        Map<String, String> paramDef = TagUtil.getParams( tagCell.getStringCellValue());

        // パラメータチェック
        checkParam( paramDef, tagCell);

        String tag = tagCell.getStringCellValue();
        ReportsParserInfo info = ( ReportsParserInfo) data;
        ParamInfo paramInfo = info.getParamInfo();
        ParsedReportInfo parsedReportInfo = new ParsedReportInfo();

        // 置換する値
        Object[] paramValues = null;
        try {
            // 置換変数の取得
            String replaceParam = paramDef.get( PARAM_VALUE);

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

            // 重複非表示の設定有無
            boolean duplicate = false;
            if ( paramDef.containsKey( PARAM_DUPLICATE)) {
                duplicate = Boolean.valueOf( paramDef.get( PARAM_DUPLICATE));
            }

            // システム変数
            if ( ReportsUtil.VALUE_SHEET_NAMES.equals( replaceParam)) {
                // シート名
                paramValues = ReportsUtil.getSheetNames( info.getReportBook()).toArray();
            } else if ( ReportsUtil.VALUE_SHEET_VALUES.equals( replaceParam)) {
                // シート値
                paramValues = ReportsUtil.getSheetValues( info.getReportBook(), propertyName, info.getReportParsers()).toArray();
            } else {
                // 置換する値を取得
                if ( paramInfo != null) {
                    paramValues = getParamData( paramInfo, replaceParam);
                }
            }

            if ( paramValues == null || paramValues.length == 0) {
                // 空文字置換
                paramValues = new Object[] {null};
            }

            // パラメータ重複判定
            if ( duplicate && paramValues.length > 1) {
                List<Object> paramValuesList = new ArrayList<Object>();
                for ( int i = 0; i <= paramValues.length - 1; i++) {
                    // 重複があったら空を追加
                    if ( !paramValuesList.contains( paramValues[i])) {
                        paramValuesList.add( paramValues[i]);
                    } else {
                        paramValuesList.add( null);
                    }
                }
                paramValues = paramValuesList.toArray();
            }

            int shiftNum = paramValues.length;
            if ( repeatNum != null && repeatNum < shiftNum) {
                shiftNum = repeatNum;
            }

            // １つであれば、シフトしない
            if ( shiftNum > 1) {
                // 値の数分シフト
                CellRangeAddress rangeAddress = new CellRangeAddress( tagCell.getRowIndex(), tagCell.getRowIndex(), tagCell.getColumnIndex(), tagCell.getColumnIndex() + shiftNum - 2);
                PoiUtil.insertRangeRight( sheet, rangeAddress);

                int tagCellWidth = sheet.getColumnWidth( tagCell.getColumnIndex());
                for ( int i = tagCell.getColumnIndex() + 1; i <= tagCell.getColumnIndex() + shiftNum - 2; i++) {
                    int colWidth = sheet.getColumnWidth( i);
                    if ( colWidth < tagCellWidth) {
                        sheet.setColumnWidth( i, tagCellWidth);
                    }
                }
            }

            Workbook workbook = sheet.getWorkbook();
            String sheetName = workbook.getSheetName( workbook.getSheetIndex( sheet));
            //シート名
            List<String> sheetNames = ReportsUtil.getSheetNames( info.getReportBook());
            //変換値
            List<Object> resultValues = new ArrayList<Object>();
            //前変換値
            Object beforeValue = null;
            for ( int i = 0; i < shiftNum; i++) {
                Row row = sheet.getRow( tagCell.getRowIndex());
                Cell cell = row.getCell( tagCell.getColumnIndex() + i);
                if ( cell == null) {
                    cell = row.createCell( tagCell.getColumnIndex() + i);
                }
                // コピー(スタイル情報の設定)
                PoiUtil.copyCell( tagCell, cell);
                //置換
                Object value = null;
                if ( beforeValue == null || !(duplicate && beforeValue.equals( paramValues[i]))) {
                    value = paramValues[i];
                    beforeValue = paramValues[i];
                }
                if ( log.isDebugEnabled()) {
                    log.debug( "[シート名=" + sheetName + ",セル=(" + cell.getRowIndex() + "," + cell.getColumnIndex() + ")]  " + tag + " ⇒ " + paramValues[i]);
                }
                // 置換
                PoiUtil.setCellValue( cell, value);
                resultValues.add(value);
                // リンク判定
                if ( sheetLink) {
                    if ( i < sheetNames.size()) {
                        PoiUtil.setHyperlink( cell, Hyperlink.LINK_DOCUMENT, "'" + sheetNames.get( i) + "'!A1");
                        if ( log.isDebugEnabled()) {
                            log.debug( "[シート名=" + sheetName + ",セル=(" + cell.getRowIndex() + "," + cell.getColumnIndex() + ")]  Hyperlink ⇒ " + "'" + sheetNames.get( i) + "'!A1");
                        }
                    }
                }
            }

            parsedReportInfo.setDefaultRowIndex( tagCell.getRowIndex());
            parsedReportInfo.setDefaultColumnIndex( tagCell.getColumnIndex());
            parsedReportInfo.setRowIndex( tagCell.getRowIndex());
            parsedReportInfo.setColumnIndex( tagCell.getColumnIndex() + shiftNum - 1);
            parsedReportInfo.setParsedObject( resultValues);
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
     * @param paramDef
     * @param tagCell
     */
    private void checkParam( Map<String, String> paramDef, Cell tagCell) {
        // キー
        if ( paramDef.containsKey( PARAM_DUPLICATE) && paramDef.containsKey( PARAM_SHEET_LINK)) {
            throw new ParseException( tagCell, "二重定義：" + PARAM_DUPLICATE + "," + PARAM_SHEET_LINK);
        }
    }

}
