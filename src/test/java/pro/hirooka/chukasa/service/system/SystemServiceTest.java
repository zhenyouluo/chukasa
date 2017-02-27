package pro.hirooka.chukasa.service.system;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pro.hirooka.chukasa.domain.service.chukasa.ISystemService;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SystemServiceTest {

    @Autowired
    ISystemService systemService;

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
        boolean bool = systemService.isEpgdump();
        assertThat(bool, is(true));
    }

    @Ignore
    @Test
    public void isMongoDB(){
        boolean bool = systemService.isMongoDB();
        assertThat(bool, is(true));
    }
}
