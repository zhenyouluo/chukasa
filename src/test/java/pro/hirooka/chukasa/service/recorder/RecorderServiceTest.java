package pro.hirooka.chukasa.service.recorder;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pro.hirooka.chukasa.Application;
import pro.hirooka.chukasa.domain.recorder.ReservedProgram;

import javax.annotation.PostConstruct;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebIntegrationTest
public class RecorderServiceTest {

    @Autowired
    IRecorderService recorderService;

    @Before
    public void setup(){
        ReservedProgram reservedProgram = new ReservedProgram();
    }

    @Ignore
    @Test
    public void test(){
        recorderService.create(null);
    }
}
