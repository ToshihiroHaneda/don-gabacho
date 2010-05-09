package jp.co.ziro.report.controller;

import org.slim3.controller.Controller;
import org.slim3.controller.Navigation;

/**
 * メニューの表示
 * @author z001
 */
public class IndexController extends Controller {

    @Override
    public Navigation run() throws Exception {
        return forward("index.jsp");
    }
}
