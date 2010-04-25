package jp.co.ziro.report.controller;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.List;

import jp.co.ziro.report.model.Template;

import org.bbreak.excella.reports.exporter.ExcelExporter;
import org.bbreak.excella.reports.model.ReportBook;
import org.bbreak.excella.reports.model.ReportSheet;
import org.bbreak.excella.reports.processor.ReportProcessor;
import org.slim3.controller.Controller;
import org.slim3.controller.Navigation;
import org.slim3.datastore.Datastore;

public class ExportController extends Controller {

    @Override
    public Navigation run() throws Exception {
        
        List<Template> files = Datastore.query(Template.class).asList();
        Template template = files.get(0);

        ByteArrayInputStream inStream = new ByteArrayInputStream(template.getBytes());
        
        ReportBook outputBook = new ReportBook(inStream,ExcelExporter.FORMAT_TYPE);
        
        ReportSheet outputSheet = new ReportSheet("テンプレート","請求書");
        
        //-----------------------------------------------------------

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
