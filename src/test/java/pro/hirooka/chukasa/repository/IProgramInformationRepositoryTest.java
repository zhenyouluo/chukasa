package pro.hirooka.chukasa.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pro.hirooka.chukasa.Application;
import pro.hirooka.chukasa.configuration.ChukasaConfiguration;
import pro.hirooka.chukasa.domain.ProgramInformation;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebIntegrationTest
public class IProgramInformationRepositoryTest {

    @Autowired
    ChukasaConfiguration chukasaConfiguration;

    @Autowired
    IProgramInformationRepository programInformationRepository;

    @Test
    public void test(){
        Integer[] physicalChannelArray = chukasaConfiguration.getPhysicalChannel();
        assertThat(physicalChannelArray.length, is(42));
        List<ProgramInformation> programInformationList = programInformationRepository.findAllByBeginDateLike("");
        log.info("size = {}", programInformationList.size());
        assertThat(true, is(true));
    }
}
