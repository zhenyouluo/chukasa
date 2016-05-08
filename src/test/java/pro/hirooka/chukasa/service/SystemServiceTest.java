package pro.hirooka.chukasa.service;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pro.hirooka.chukasa.Application;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebIntegrationTest
public class SystemServiceTest {

    @Autowired
    SystemService systemService;

    @Ignore
    @Test
    public void test(){
        String webCameraDeviceName = systemService.getWebCameraDeviceName();
        assertThat(webCameraDeviceName, is("/dev/video0"));
    }

    @Ignore
    @Test
    public void mogmog(){
        File file = new File(systemService.getWebCameraDeviceName());
        boolean bool = file.exists();
        assertThat(bool, is(true));
    }

    @Ignore
    @Test
    public void isFPGDump(){
        boolean bool = systemService.isEPGDump();
        assertThat(bool, is(true));
    }
}
