package jp.co.ziro.report.controller;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import jp.co.ziro.report.model.Template;

import org.bbreak.excella.reports.exporter.ExcelExporter;
import org.bbreak.excella.reports.model.ReportBook;
import org.bbreak.excella.reports.model.ReportSheet;
import org.bbreak.excella.reports.processor.ReportProcessor;
import org.bbreak.excella.reports.tag.RowRepeatParamParser;
import org.bbreak.excella.reports.tag.SingleParamParser;
import org.slim3.controller.Controller;
import org.slim3.controller.Navigation;
import org.slim3.datastore.Datastore;

public class ExportController extends Controller {

    @Override
    public Navigation run() throws Exception {
        
        List<Template> files = Datastore.query(Template.class).asList();
        Template template = files.get(0);
        
        System.out.println(template.getBytes().length);

        ByteArrayInputStream inStream = new ByteArrayInputStream(template.getBytes());
        
        ReportBook outputBook = new ReportBook(inStream,ExcelExporter.FORMAT_TYPE);
        
        ReportSheet outputSheet = new ReportSheet("テンプレート","請求書");
        
        //-----------------------------------------------------------
        /**
         * エンジン部分
         */
        outputSheet.addParam( SingleParamParser.DEFAULT_TAG, "請求日付","2010/4/30");
        outputSheet.addParam( SingleParamParser.DEFAULT_TAG, "請求番号","REQ-201004xxxxxx");
        outputSheet.addParam( SingleParamParser.DEFAULT_TAG, "顧客名称","株式会社ドッグラン");
        outputSheet.addParam( SingleParamParser.DEFAULT_TAG, "顧客郵便番号","〒420-0032");
        outputSheet.addParam( SingleParamParser.DEFAULT_TAG, "顧客住所1","静岡県静岡市葵区両替町２丁目３番地６");
        outputSheet.addParam( SingleParamParser.DEFAULT_TAG, "顧客住所2","大原ビル２Ｆ");
        outputSheet.addParam( SingleParamParser.DEFAULT_TAG, "顧客電話番号","050-5538-4671");
        outputSheet.addParam( SingleParamParser.DEFAULT_TAG, "顧客FAX番号","-");

        List<String> productNameList = new ArrayList<String>();
        productNameList.add( "ついーとカフェ開発費");
        productNameList.add( "ついーとカフェ開発費");
        outputSheet.addParam(RowRepeatParamParser.DEFAULT_TAG, "商品名", productNameList.toArray());

        List<String> priceList = new ArrayList<String>();
        priceList.add("10000000");
        priceList.add("10000000");
        outputSheet.addParam(RowRepeatParamParser.DEFAULT_TAG, "単価", priceList.toArray());

        List<String> unitList = new ArrayList<String>();
        unitList.add("1");
        unitList.add("1");
        outputSheet.addParam(RowRepeatParamParser.DEFAULT_TAG, "数量", unitList.toArray());


        //-----------------------------------------------------------

        outputBook.addReportSheet(outputSheet);

        ReportProcessor reportProcessor = new ReportProcessor();
        reportProcessor.process(outputBook);
        

        response.setHeader("Content-Disposition","attachment; filename=excel.xls");
        response.setContentType("application/msexcel");
 
        OutputStream out = new BufferedOutputStream(response.getOutputStream());
        out.write(outputBook.getBytes());
        out.close();
        
        return null;
    }
}
