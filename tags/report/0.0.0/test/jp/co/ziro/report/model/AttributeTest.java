package jp.co.ziro.report.model;

import org.slim3.tester.AppEngineTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class AttributeTest extends AppEngineTestCase {

    private Param model = new Param();

    @Test
    public void test() throws Exception {
        assertThat(model, is(notNullValue()));
    }
}
