package jp.co.ziro.report.controller.report;

import java.util.List;

import jp.co.ziro.report.model.Param;
import jp.co.ziro.report.model.RepeatParam;
import jp.co.ziro.report.model.Report;
import jp.co.ziro.report.model.ReportParam;
import jp.co.ziro.report.model.ReportRepeatParam;
import jp.co.ziro.report.model.Template;
import jp.co.ziro.report.service.TemplateService;

import org.slim3.controller.Controller;
import org.slim3.controller.Navigation;
import org.slim3.datastore.Datastore;

import com.google.appengine.repackaged.com.google.common.base.StringUtil;

public class InputExecController extends Controller {

    @Override
    public Navigation run() throws Exception {

        String idBuf = requestScope("templateId");
        //指定された帳票を検索
        Template template = TemplateService.findById(Integer.valueOf(idBuf));

        //テンプレートを元にデータを作成する
        Report report = new Report();

        String name = requestScope("reportName");
        String detail = requestScope("reportDetail");
        report.setName(name);
        report.setDetail(detail);
        report.getTemplateRef().setModel(template);
        //テンプレートと関連付け
        Datastore.put(template,report);

        //テンプレートを元にオブジェクトを作成
        createParam(template,report);
        createRepeatParam(template,report);

        return redirect("/report/view?templateId=" + idBuf);
    }

    private void createRepeatParam(Template template, Report report) {
       
        if ( template.getRepeatParamListRef() == null ) {
            return;
        }
        List<RepeatParam> repeatParamList = template.getRepeatParamListRef().getModelList();
        
        for ( RepeatParam repeatParam : repeatParamList ) {

            //関連付けして追加
            ReportRepeatParam rereParam = new ReportRepeatParam();
            rereParam.getRepeatParamRef().setModel(repeatParam);
            rereParam.getReportRef().setModel(report);
            Datastore.put(rereParam,repeatParam,report);
 
            int rowNum = 1;
            while( true ) {

                boolean emptyFlag = true;
                List<Param> paramList = repeatParam.getParamListRef().getModelList();
                for ( Param param : paramList ) {
                    String formName = repeatParam.getSeq() + "_" + param.getSeq() + "_" + rowNum;
                    String value = requestScope(formName);
                    if ( StringUtil.isEmpty(value) ) {
                        continue;
                    }
                    emptyFlag = false;
                    
                    ReportParam reportParam = new ReportParam();
                    reportParam.setValue(value);
                    reportParam.setRowNum(rowNum);
                    reportParam.getParamRef().setModel(param);
                    reportParam.getReportRepeatParamRef().setModel(rereParam);
                    Datastore.put(reportParam,rereParam,param);
                }

                if ( emptyFlag ) break;
                ++rowNum;
            }
        }
    }

    private void createParam(Template template, Report report) {
        if ( template.getParamListRef() == null ) {
            return;
        }
        List<Param> paramList = template.getParamListRef().getModelList();
        for ( Param param : paramList ) {
            String formName = String.valueOf(param.getSeq());
            String value = requestScope(formName);
            if ( StringUtil.isEmpty(value) ) {
                continue;
            }
            //関連付けして追加
            ReportParam reParam = new ReportParam();
            reParam.setValue(value);
            reParam.getParamRef().setModel(param);
            reParam.getReportRef().setModel(report);
            Datastore.put(reParam,template,report);
        }
        return;
    }
}
