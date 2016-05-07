package pro.hirooka.chukasa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.EPGDumpChannelInformation;

import javax.annotation.PostConstruct;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class EPGDumpService implements IEPGDumpService {

    @PostConstruct
    public void init(){

        Resource resource = new ClassPathResource("epgdump_channel_map.json");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Integer> epgdumpChannelMap = objectMapper.readValue(resource.getFile(), HashMap.class);
            log.info(epgdumpChannelMap.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 20 */3 * * *")
    void execute(){

    }
}
