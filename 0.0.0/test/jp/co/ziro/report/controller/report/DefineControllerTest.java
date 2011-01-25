package jp.co.ziro.report.controller.report;

import org.slim3.tester.ControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class DefineControllerTest extends ControllerTestCase {

    @Test
    public void run() throws Exception {
        tester.start("/report/define");
        DefineController controller = tester.getController();
        assertThat(controller, is(notNullValue()));
        assertThat(tester.isRedirect(), is(false));
        assertThat(tester.getDestinationPath(), is("/report/define.jsp"));
    }
}
