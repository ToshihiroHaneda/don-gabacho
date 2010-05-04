package jp.co.ziro.report.controller.report;

import java.util.List;

import jp.co.ziro.report.model.Report;
import jp.co.ziro.report.service.TemplateService;

import org.slim3.controller.Controller;
import org.slim3.controller.Navigation;

public class ViewController extends Controller {

    @Override
    public Navigation run() throws Exception {
        String idBuf = requestScope("templateId");
        //帳票検索
        List<Report> reportList = TemplateService.findReportList(Integer.valueOf(idBuf));
        requestScope("reportList",reportList);        

        return forward("view.jsp");
    }
}
