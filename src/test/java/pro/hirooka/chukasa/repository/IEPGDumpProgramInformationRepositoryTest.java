package pro.hirooka.chukasa.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pro.hirooka.chukasa.Application;
import pro.hirooka.chukasa.configuration.ChukasaConfiguration;
import pro.hirooka.chukasa.domain.EPGDumpProgramInformation;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebIntegrationTest
public class IEPGDumpProgramInformationRepositoryTest {

    @Autowired
    ChukasaConfiguration chukasaConfiguration;

    @Autowired
    IEPGDumpProgramInformationRepository epgDumpProgramInformationRepository;

    @Ignore
    @Test
    public void findAll() throws ParseException {
        long now = new Date().getTime();
        log.info("now = {}", now);
        List<EPGDumpProgramInformation> epgDumpProgramInformationList =
                epgDumpProgramInformationRepository.findAll();
        log.info("{}", epgDumpProgramInformationList.toString());
        log.info("size of list = {}", epgDumpProgramInformationList.size());
    }

    @Ignore
    @Test
    public void findAllByOneChannel() throws ParseException {
        long now = new Date().getTime();
        log.info("now = {}", now);
        String channel = "GR1_1024";
        List<EPGDumpProgramInformation> epgDumpProgramInformationList =
                epgDumpProgramInformationRepository.findAllByChannel(channel);
        log.info("{}", epgDumpProgramInformationList.toString());
        log.info("size of list = {}", epgDumpProgramInformationList.size());
    }

    @Ignore
    @Test
    public void findAllByChannel() throws IOException {
        Resource resource = new ClassPathResource("epgdump_channel_map.json");
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Integer> epgdumpChannelMap = objectMapper.readValue(resource.getFile(), HashMap.class);
        log.info(epgdumpChannelMap.toString());
        for(Map.Entry<String, Integer> entry : epgdumpChannelMap.entrySet()) {
            log.info("key = {}, value = {}", entry.getKey(), entry.getValue());
            List<EPGDumpProgramInformation> epgDumpProgramInformationList =
                    epgDumpProgramInformationRepository.findAllByChannel(entry.getKey());
            log.info("{}", epgDumpProgramInformationList.toString());
            log.info("size of list = {}", epgDumpProgramInformationList.size());
        }
    }

    @Ignore
    @Test
    public void findOneByChannelAndNowLike() throws IOException {
        Resource resource = new ClassPathResource("epgdump_channel_map.json");
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Integer> epgdumpChannelMap = objectMapper.readValue(resource.getFile(), HashMap.class);
        log.info(epgdumpChannelMap.toString());

        long now = new Date().getTime() * 10;
        for(Map.Entry<String, Integer> entry : epgdumpChannelMap.entrySet()) {
            log.info("key = {}, value = {}", entry.getKey(), entry.getValue());
            EPGDumpProgramInformation epgDumpProgramInformation =
                    epgDumpProgramInformationRepository.findOneByChannelAndNowLike(entry.getKey(), now);
            log.info("{}", epgDumpProgramInformation.toString());
        }
    }

    @Ignore
    @Test
    public void findOneByNowLike() {
        long now = new Date().getTime() * 10;
        List<EPGDumpProgramInformation> epgDumpProgramInformationList =
                epgDumpProgramInformationRepository.findAllByNowLike(now);
        epgDumpProgramInformationList.forEach(epgDumpProgramInformation -> {
            log.info("{}", epgDumpProgramInformation.toString());
        });
        log.info("size of list = {}", epgDumpProgramInformationList.size());
    }
}
