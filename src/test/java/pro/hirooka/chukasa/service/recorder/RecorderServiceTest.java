package pro.hirooka.chukasa.service.recorder;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pro.hirooka.chukasa.domain.model.recorder.ReservedProgram;
import pro.hirooka.chukasa.domain.service.recorder.IRecorderService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
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
