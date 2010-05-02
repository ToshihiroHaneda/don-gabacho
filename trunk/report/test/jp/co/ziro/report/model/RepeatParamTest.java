package jp.co.ziro.report.model;

import org.slim3.tester.AppEngineTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class RepeatParamTest extends AppEngineTestCase {

    private RepeatParam model = new RepeatParam();

    @Test
    public void test() throws Exception {
        assertThat(model, is(notNullValue()));
    }
}
