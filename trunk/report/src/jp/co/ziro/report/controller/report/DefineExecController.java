package jp.co.ziro.report.controller.report;

import jp.co.ziro.report.model.Param;
import jp.co.ziro.report.model.RepeatParam;
import jp.co.ziro.report.model.Template;

import org.slim3.controller.Controller;
import org.slim3.controller.Navigation;
import org.slim3.controller.upload.FileItem;
import org.slim3.datastore.Datastore;

import com.google.appengine.repackaged.com.google.common.base.StringUtil;

public class DefineExecController extends Controller {

    @Override
    public Navigation run() throws Exception {
        
        //TODO トランザクション
       
        Template template = createTemplate();
        createParam(template);
        createRepeatParam(template);
        
        return redirect("/report/");
    }

    /**
     * テンプレートの作成
     * @return 作成したテンプレート
     */
    private Template createTemplate() {
        
        Template template = new Template();
        template.setKey(Datastore.allocateId(Template.class));
        
        template.setName((String)requestScope("name"));
        template.setDetail((String)requestScope("detail"));

        FileItem formFile = requestScope("templateFile"); 
        template.setBytes(formFile.getData());
 
        return template;
    }

    /**
     * 帳票パラメータの設定
     * @param template
     */
    private void createParam(Template template) {
        int cnt = 1;
        while (true) {
            String name = requestScope("paramName" + cnt);
            if ( StringUtil.isEmpty(name) ) {
                break;
            }
            String type = requestScope("paramType" + cnt);
            String attr = requestScope("paramAttr" + cnt);
            
            Param param = new Param();
           
            param.setName(name);
            param.setType(Integer.valueOf(type));
            param.setAttr(attr);
            param.setSeq(cnt);

            param.getTemplateRef().setModel(template);
            Datastore.put(param, template);
            ++cnt;
        }
    }

    /**
     * リスト用のパラメータの設定
     * @param template
     */
    private void createRepeatParam(Template template) {
        int cnt = 1;
        while (true) {
            String name = requestScope("repeatName" + cnt);
            if ( StringUtil.isEmpty(name) ) {
                break;
            }
            String detail = requestScope("repeatDetail" + cnt);

            RepeatParam repeatParam = new RepeatParam();
            repeatParam.setName(name);
            repeatParam.setDetail(detail);
            repeatParam.setSeq(cnt);
            repeatParam.getTemplateRef().setModel(template);
           	Datastore.put(repeatParam, template);

            int paramCnt = 1;
            while ( true ) {
                String paramName = requestScope("repeatParam" + cnt + "Name" + paramCnt);
                if ( StringUtil.isEmpty(paramName) ) {
                    break;
                }

                String paramType = requestScope("repeatParam" + cnt + "Type" + paramCnt);
                String paramAttr = requestScope("repeatParam" + cnt + "Attr" + paramCnt);

            	Param param = new Param();

            	param.setName(paramName);
            	param.setType(Integer.valueOf(paramType));
            	param.setAttr(paramAttr);
                param.setSeq(paramCnt);

            	param.getRepeatParamRef().setModel(repeatParam);
                Datastore.put(param, repeatParam);
 
            	++paramCnt;
            }
            ++cnt;
        }
    }
}
