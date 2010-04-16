/*************************************************************************
 *
 * Copyright 2009 by bBreak Systems.
 *
 * ExCella Reports - Excelファイルを利用した帳票ツール
 *
 * $Id: ReportProcessor.java 83 2009-10-30 04:02:20Z a-hoshino $
 * $Revision: 83 $
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
package org.bbreak.excella.reports.processor;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.bbreak.excella.core.BookController;
import org.bbreak.excella.core.BookData;
import org.bbreak.excella.core.SheetData;
import org.bbreak.excella.core.exporter.book.BookExporter;
import org.bbreak.excella.reports.exporter.ReportBookExporter;
import org.bbreak.excella.reports.listener.RemoveAdapter;
import org.bbreak.excella.reports.listener.ReportProcessListener;
import org.bbreak.excella.reports.model.ConvertConfiguration;
import org.bbreak.excella.reports.model.ReportBook;
import org.bbreak.excella.reports.model.ReportSheet;
import org.bbreak.excella.reports.tag.ReportsTagParser;
import org.bbreak.excella.reports.util.ReportsUtil;

/**
 * 帳票生成プロセッサ
 *
 * @since 1.0
 */
public class ReportProcessor {

    /**
     * タグパーサーMap<BR>
     * キー：タグ名<BR>
     * 値：処理する帳票用タグパーサー
     */
    private Map<String, ReportsTagParser<?>> parsers = new HashMap<String, ReportsTagParser<?>>();

    /**
     * エクスポーターMap<BR>
     * キー：出力タイプ<BR>
     * 値：処理するエクスポーター
     */
    private Map<String, ReportBookExporter> exporters = new HashMap<String, ReportBookExporter>();

    /**
     * リスナー
     */
    private List<ReportProcessListener> listeners = new ArrayList<ReportProcessListener>();

    /**
     * コンストラクタ
     */
    public ReportProcessor() {
        // デフォルトのタグパーサーの作成
        // Parser作成
        parsers.putAll( ReportCreateHelper.createDefaultParsers());

        // デフォルトのエクスポーターの作成
        // Exporter作成
        exporters.putAll( ReportCreateHelper.createDefaultExporters());
    }

    /**
     * 変換処理を実行する。
     *
     * @param reportBooks 帳票情報
     * @throws IOException ファイルの読み込みに失敗した場合
     * @throws ParseException 変換処理に失敗した場合
     * @throws ExportException 出力処理に失敗した場合
     */
    public void process( ReportBook... reportBooks) throws Exception {
        // 出力ブックデータ毎に処理する
        for ( ReportBook reportBook : reportBooks) {
            processBook( reportBook);
        }
    }

    /**
     * ワークブックの変換処理を実行する。
     *
     * @param reportBook ワークブックの置換情報
     * @throws IOException ファイルの読み込みに失敗した場合
     * @throws ParseException 変換処理に失敗した場合
     * @throws ExportException 出力処理に失敗した場合
     */
    private void processBook( ReportBook reportBook) throws Exception {

        if ( reportBook == null) {
            return;
        }

        Workbook workbook = getTemplateWorkbook( reportBook.getTemplateFileName());

        for ( ReportProcessListener listener : listeners) {
            listener.preBookParse( workbook, reportBook);
        }

        checkReportBook( reportBook);

        // テンプレート展開
        Set<Integer> delTemplateIndexs = expandTemplate( workbook, reportBook);

        // 出力ファイル取得
        BookController controller = new BookController( workbook);

        // Parserの設定
        for ( ReportsTagParser<?> tagParser : parsers.values()) {
            controller.addTagParser( tagParser);
        }
        // リスナーの設定
        controller.addSheetParseListener( new RemoveAdapter());
        for ( ReportProcessListener listener : listeners) {
            controller.addSheetParseListener( listener);
        }

        // Exporterの設定
        for ( ConvertConfiguration configuration : reportBook.getConfigurations()) {
            if ( configuration == null) {
                continue;
            }
            for ( ReportBookExporter reportExporter : exporters.values()) {
                if ( configuration.getFormatType().equals( reportExporter.getFormatType())) {
                    reportExporter.setConfiguration( configuration);
                    reportExporter.setFilePath( reportBook.getOutputFileName() + reportExporter.getExtention());
                    controller.addBookExporter( reportExporter);
                }
            }
        }

        ReportsParserInfo reportsParserInfo = new ReportsParserInfo();
        reportsParserInfo.setReportParsers( new ArrayList<ReportsTagParser<?>>( parsers.values()));
        reportsParserInfo.setReportBook( reportBook);

        BookData bookData = controller.getBookData();
        bookData.clear();
        for ( String sheetName : controller.getSheetNames()) {
            if ( sheetName.startsWith( BookController.COMMENT_PREFIX)) {
                continue;
            }
            ReportSheet reportSheet = ReportsUtil.getReportSheet( sheetName, reportBook);
            if ( reportSheet != null) {

                reportsParserInfo.setParamInfo( reportSheet.getParamInfo());

                // 解析の実行
                SheetData sheetData = controller.parseSheet( sheetName, reportsParserInfo);
                // 結果の追加
                controller.getBookData().putSheetData( sheetName, sheetData);
            }
        }

        // 不要テンプレート削除
        for ( Integer deleteSheetIndex : delTemplateIndexs) {
            workbook.removeSheetAt( deleteSheetIndex);
        }

        // 出力処理前にリスナー呼び出し
        for ( ReportProcessListener listener : listeners) {
            listener.postBookParse( workbook, reportBook);
        }

        // 出力処理の実行
        for ( BookExporter exporter : controller.getExporter()) {
            if ( exporter != null) {
                exporter.setup();
                try {
                    exporter.export( workbook, bookData);
                } finally {
                    exporter.tearDown();
                }
            }
        }
    }

    /**
     * ワークブックをチェックする
     *
     * @param reportBook ワークブックの置換情報
     */
    private void checkReportBook( ReportBook reportBook) {
    }

    /**
     * テンプレートのワークブックを取得する。
     *
     * @param filepath テンプレートファイルパス
     * @return テンプレートワークブック
     * @throws IOException ファイルの読み込みに失敗した場合
     */
    private Workbook getTemplateWorkbook( String filepath) throws Exception {

        FileInputStream fileIn = null;
        Workbook wb = null;

        try {
            fileIn = new FileInputStream( filepath);
            wb = WorkbookFactory.create( fileIn);
        } finally {
            if ( fileIn != null) {
                fileIn.close();
            }
        }

        return wb;
    }

    /**
     * タグパーサーを追加する。
     *
     * @param tagParser 追加するタグパーサー
     */
    public void addReportsTagParser( ReportsTagParser<?> tagParser) {
        parsers.put( tagParser.getTag(), tagParser);
    }

    /**
     * タグのタグパーサーを削除する。
     *
     * @param tag タグ
     */
    public void removeReportsTagParser( String tag) {
        parsers.remove( tag);
    }

    /**
     * すべてのタグパーサーを削除する。
     */
    public void clearReportsTagParser() {
        parsers.clear();
    }

    /**
     * エクスポーターを追加する。
     *
     * @param exporter 追加するエクスポーター
     */
    public void addReportBookExporter( ReportBookExporter exporter) {
        exporters.put( exporter.getFormatType(), exporter);
    }

    /**
     * フォーマットタイプのエクスポーターを削除する。
     *
     * @param formatType フォーマットタイプ
     */
    public void removeReportBookExporter( String formatType) {
        exporters.remove( formatType);
    }

    /**
     * すべてのエクスポーターを削除する。
     */
    public void clearReportBookExporter() {
        exporters.clear();
    }

    /**
     * リスナーを追加する。
     *
     * @param listener リスナー
     */
    public void addReportProcessListener( ReportProcessListener listener) {
        listeners.add( listener);
    }

    /**
     * リスナーを削除する。
     *
     * @param listener リスナー
     */
    public void removeReportProcessListener( ReportProcessListener listener) {
        listeners.remove( listener);
    }

    /**
     * テンプレートワークブックのシートを、帳票出力単位に変換する。
     *
     * @param workbook テンプレートワークブック
     * @param reportBook 帳票ワークブック情報
     * @return 削除が必要なテンプレートシートインデックス
     */
    private Set<Integer> expandTemplate( Workbook workbook, ReportBook reportBook) {

        Set<Integer> delTemplateIndexs = new TreeSet<Integer>( Collections.reverseOrder());
        Set<Integer> useTemplateIndexs = new HashSet<Integer>();

        // 出力シート単位にコピーする
        for ( ReportSheet reportSheet : reportBook.getReportSheets()) {

            if ( reportSheet != null) {
                if ( reportSheet.getSheetName().equals( reportSheet.getTemplateName())) {
                    // テンプレート名＝出力シート名
                    int lastSheetIndex = workbook.getNumberOfSheets() - 1;
                    workbook.setSheetOrder( reportSheet.getSheetName(), lastSheetIndex);
                    useTemplateIndexs.add( lastSheetIndex);
                } else {
                    int tempIdx = workbook.getSheetIndex( reportSheet.getTemplateName());

                    Sheet sheet = workbook.cloneSheet( tempIdx);
                    workbook.setSheetName( workbook.getSheetIndex( sheet), reportSheet.getSheetName());
                    delTemplateIndexs.add( tempIdx);
                }
            }
        }

        // 出力対象外シートを削除インデックスに追加
        for ( int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt( i);
            if ( !isOutputSheet( sheet, reportBook)) {
                delTemplateIndexs.add( workbook.getSheetIndex( sheet));
            }
        }

        delTemplateIndexs.removeAll( useTemplateIndexs);

        return delTemplateIndexs;

    }

    /**
     * 出力対象のシートか判定する
     *
     * @param sheet テンプレートシート
     * @param reportBook 帳票ワークブック情報
     * @return
     */
    private boolean isOutputSheet( Sheet sheet, ReportBook reportBook) {
        for ( ReportSheet reportSheet : reportBook.getReportSheets()) {
            if ( reportSheet != null && sheet.getSheetName().equals( reportSheet.getSheetName())) {
                return true;
            }
        }
        return false;
    }

}
