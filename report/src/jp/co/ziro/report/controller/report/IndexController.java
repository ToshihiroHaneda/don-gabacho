package jp.co.ziro.report.controller.report;

import java.util.List;

import jp.co.ziro.report.model.Template;
import jp.co.ziro.report.service.TemplateService;

import org.slim3.controller.Controller;
import org.slim3.controller.Navigation;

public class IndexController extends Controller {

    @Override
    public Navigation run() throws Exception {
        //定義検索
        List<Template> templateList = TemplateService.findTemplateList();
        requestScope("templateList",templateList);
        return forward("index.jsp");
    }
}
