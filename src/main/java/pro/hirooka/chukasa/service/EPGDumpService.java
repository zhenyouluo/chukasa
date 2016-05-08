package pro.hirooka.chukasa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.configuration.ChukasaConfiguration;
import pro.hirooka.chukasa.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.LastEPGDumpExecuted;
import pro.hirooka.chukasa.epgdump.EPGDumpRunner;
import pro.hirooka.chukasa.epgdump.IEPGDumpParser;

import javax.annotation.PostConstruct;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
public class EPGDumpService implements IEPGDumpService {

    private final SystemConfiguration systemConfiguration;
    private final ChukasaConfiguration chukasaConfiguration;
    private final IEPGDumpParser epgDumpParser;
    private final ILastEPGDumpExecutedService lastEPGDumpExecutedService;
    private final ISystemService systemService;

    @Autowired
    public EPGDumpService(
            SystemConfiguration systemConfiguration,
            ChukasaConfiguration chukasaConfiguration,
            IEPGDumpParser epgDumpParser,
            ILastEPGDumpExecutedService lastEPGDumpExecutedService,
            ISystemService systemService){
        this.systemConfiguration = requireNonNull(systemConfiguration, "systemConfiguration");
        this.chukasaConfiguration = requireNonNull(chukasaConfiguration, "chukasaConfiguration");
        this.epgDumpParser = requireNonNull(epgDumpParser, "epgDumpParser");
        this.lastEPGDumpExecutedService = requireNonNull(lastEPGDumpExecutedService, "lastEPGDumpExecutedService");
        this.systemService = requireNonNull(systemService, "systemService");
    }

    @PostConstruct
    public void init(){

        if(systemService.isEPGDump()) {
            LastEPGDumpExecuted lastEPGDumpExecuted = lastEPGDumpExecutedService.read(1);
            if (lastEPGDumpExecuted == null) {
                runEPGDump();
            } else {
                Date date = new Date();
                long now = date.getTime();
                long last = lastEPGDumpExecuted.getDate();
                log.info("{}, {}", now, last);
                if (now - last > chukasaConfiguration.getEpgdumpExecuteOnBootIgnoreInterval()) {
                    runEPGDump();
                }
            }
        }
    }

    @Scheduled(cron = "0 0 4 * * *")
    void execute(){

        if(systemService.isEPGDump()) {
            runEPGDump();
        }
    }

    void runEPGDump(){

        Resource resource = new ClassPathResource("epgdump_channel_map.json");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Integer> epgdumpChannelMap = objectMapper.readValue(resource.getFile(), HashMap.class);
            log.info(epgdumpChannelMap.toString());

            EPGDumpRunner epgDumpRunner = new EPGDumpRunner(systemConfiguration, chukasaConfiguration, epgDumpParser, lastEPGDumpExecutedService, epgdumpChannelMap);
            Thread thread = new Thread(epgDumpRunner);
            thread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
