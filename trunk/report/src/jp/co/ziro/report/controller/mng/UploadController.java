package jp.co.ziro.report.controller.mng;

import org.slim3.controller.Controller;
import org.slim3.controller.Navigation;

public class UploadController extends Controller {

    @Override
    public Navigation run() throws Exception {
        return forward("upload.jsp");
    }
}
