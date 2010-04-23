package jp.co.ziro.report.controller.mng;


import jp.co.ziro.report.meta.TemplateMeta;
import jp.co.ziro.report.model.Template;

import org.slim3.controller.Controller;
import org.slim3.controller.Navigation;
import org.slim3.controller.upload.FileItem;
import org.slim3.datastore.Datastore;

public class UploadExecController extends Controller {

    @Override
    public Navigation run() throws Exception {
        FileItem formFile = requestScope("template"); 

        Template data = new Template();
        TemplateMeta meta = new TemplateMeta();
        
        data.setKey(Datastore.allocateId(meta));
        byte[] bytes = formFile.getData();
        data.setBytes(bytes);

        Datastore.put(data);
        return forward("upload.jsp");
    }
}
