package jp.co.ziro.report.controller.report;

import org.slim3.controller.Controller;
import org.slim3.controller.Navigation;

/**
 * 定義への遷移
 * @author z001
 */
public class DefineController extends Controller {

    @Override
    public Navigation run() throws Exception {
        return forward("define.jsp");
    }
}
