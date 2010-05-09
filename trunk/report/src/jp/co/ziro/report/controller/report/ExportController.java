package jp.co.ziro.report.controller.report;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jp.co.ziro.report.model.Param;
import jp.co.ziro.report.model.Report;
import jp.co.ziro.report.model.ReportParam;
import jp.co.ziro.report.model.ReportRepeatParam;
import jp.co.ziro.report.model.Template;
import jp.co.ziro.report.service.ReportService;

import org.bbreak.excella.reports.exporter.ExcelExporter;
import org.bbreak.excella.reports.model.ReportBook;
import org.bbreak.excella.reports.model.ReportSheet;
import org.bbreak.excella.reports.processor.ReportProcessor;
import org.bbreak.excella.reports.tag.RowRepeatParamParser;
import org.bbreak.excella.reports.tag.SingleParamParser;
import org.slim3.controller.Controller;
import org.slim3.controller.Navigation;

/**
 * 出力コントローラー
 * @author z001
 */
public class ExportController extends Controller {

    @Override
    public Navigation run() throws Exception {
       
        String reportId = requestScope("reportId");
        //帳票データを取得
        Report report = ReportService.findById(Integer.valueOf(reportId));
        //テンプレートを取得
        Template template = report.getTemplateRef().getModel();
        //テンプレートのストリームを取得
        ByteArrayInputStream inStream = new ByteArrayInputStream(template.getBytes());

        //エクセルのフォーマット指定
        ReportBook outputBook = new ReportBook(inStream,ExcelExporter.FORMAT_TYPE);
        //帳票の名称に変更
        ReportSheet outputSheet = new ReportSheet("template",report.getName());

        List<ReportParam> paramList = report.getReportParamListRef().getModelList();
        addParam(paramList,outputSheet);

        List<ReportRepeatParam> repeatParamList = report.getReportRepeatParamListRef().getModelList();
        for ( ReportRepeatParam repeatParam : repeatParamList ) {
            addRepeatParam(repeatParam,outputSheet);
        }

        //シートとして追加
        outputBook.addReportSheet(outputSheet);

        ReportProcessor reportProcessor = new ReportProcessor();
        //帳票を作成
        reportProcessor.process(outputBook);

        response.setHeader("Content-Disposition","attachment; filename=excel.xls");
        response.setContentType("application/msexcel");

        //レスポンスに書き込む
        OutputStream out = new BufferedOutputStream(response.getOutputStream());
        out.write(outputBook.getBytes());
        out.close();
        
        return null;
    }

    /**
     * パラメータの追加
     * @param repeatParam
     * @param outputSheet
     */
    private void addRepeatParam(ReportRepeatParam repeatParam,ReportSheet outputSheet) {
       
        Map<Integer,List<ReportParam>> reportMap = new HashMap<Integer,List<ReportParam>>();
        //リスト数に合わせて追加する
        List<ReportParam> paramList = repeatParam.getReportParamListRef().getModelList();

        Map<Param,List<String>> paramMap = new HashMap<Param,List<String>>();
        Integer maxNum = 0;
        //ページに合わせてデータを集める
        for ( ReportParam reportParam : paramList ) {
            Integer rowNum = reportParam.getRowNum();
            if ( rowNum > maxNum ) maxNum = rowNum;
            List<ReportParam> reportList = reportMap.get(rowNum);
            //リストが存在しない場合
            if ( reportList == null ) {
                reportList = new ArrayList<ReportParam>();
                reportMap.put(rowNum, reportList);
            }
 
            //値用のリストを生成しておく
            Param param = reportParam.getParamRef().getModel();
            List<String> valueList = paramMap.get(param);
            if ( valueList == null ) {
                valueList = new ArrayList<String>();
                paramMap.put(param, valueList);
            }
            //パラムをリストに設定しておく
            reportList.add(reportParam);
        }

        //行数回繰り返してリストに設定
        for ( int cnt = 1; cnt <= maxNum; ++cnt ) {

            List<ReportParam> reportParamList = reportMap.get(cnt);
            //パラメータの設定
            for ( ReportParam reportParam : reportParamList ) {
                Param param = reportParam.getParamRef().getModel();
                List<String> valueList = paramMap.get(param);
                valueList.add(reportParam.getValue());
            }

            //存在しないパラメータで空文字を入れておく
            Iterator<Entry<Param, List<String>>> itr = paramMap.entrySet().iterator();
            while (itr.hasNext()) {
                Entry<Param, List<String>> entry = itr.next();
                List<String> valueList = entry.getValue();
                //総数が違った場合
                if ( valueList.size() != cnt ) {
                    valueList.add("");
                }
            }
        }

        //蓄えたデータ数回繰り返す
        Iterator<Entry<Param, List<String>>> itr = paramMap.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<Param, List<String>> entry = itr.next();
            Param param = entry.getKey();
            List<String> valueList = entry.getValue();
            //行としてシートに追加
            outputSheet.addParam(RowRepeatParamParser.DEFAULT_TAG,param.getName(), valueList.toArray());
        }
    }

    /**
     * パラムデータの追加
     * @param paramList パラメータデータリスト
     * @param outputSheet 出力対象シート
     */
    private void addParam(List<ReportParam> paramList, ReportSheet outputSheet) {
        //パラメータ数回繰り返す
        for (ReportParam reportParam : paramList ) {
            Param param = reportParam.getParamRef().getModel();
            //パラメータ名で値を埋め込む
            outputSheet.addParam( SingleParamParser.DEFAULT_TAG,param.getName(),reportParam.getValue());
        }
    }
}
