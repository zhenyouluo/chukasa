package pro.hirooka.chukasa.service.epgdump;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.configuration.ChukasaConfiguration;
import pro.hirooka.chukasa.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.epgdump.LastEpgdumpExecuted;
import pro.hirooka.chukasa.epgdump.EPGDumpRunner;
import pro.hirooka.chukasa.epgdump.IEpgdumpParser;
import pro.hirooka.chukasa.service.system.ISystemService;

import javax.annotation.PostConstruct;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class EpgdumpService implements IEpgdumpService {

    @Autowired
    SystemConfiguration systemConfiguration;
    @Autowired
    ChukasaConfiguration chukasaConfiguration;
    @Autowired
    IEpgdumpParser epgDumpParser;
    @Autowired
    ILastEpgdumpExecutedService lastEPGDumpExecutedService;
    @Autowired
    ISystemService systemService;

    @PostConstruct
    public void init(){

        // epgdump へのパスが存在していて，
        // 一度も情報を取得していない，あるいは前回情報を取得してから一定期間経過している場合，
        // アプリケーション起動時に別スレッドで情報を取得する．
        if(systemService.isMongoDB() && systemService.isEpgdump() && !isEPGDumpExecuted()) {
            LastEpgdumpExecuted lastEpgdumpExecuted = lastEPGDumpExecutedService.read(1);
            if (lastEpgdumpExecuted == null) {
                log.info("lastEpgdumpExecuted == null -> runEPGDump()");
                runEPGDump();
            } else {
                Date date = new Date();
                long now = date.getTime();
                long last = lastEpgdumpExecuted.getDate();
                long diff = last - now;
                log.info("now = {}, last epgdump executed = {}, diff = {}", now, last, diff);
                if (now - last > chukasaConfiguration.getEpgdumpExecuteOnBootIgnoreInterval()) {
                    runEPGDump();
                }else{
                    log.info("chukasa.epgdump-execute-on-boot-ignore-interval > previous boot");
                }
            }
        }else{
            log.info("runEPGDump() is not executed because it is running now.");
        }
    }

    @Scheduled(cron = "${chukasa.epgdump-execute-schedule-cron}")
    void execute(){

        if(systemService.isEpgdump() && systemService.isMongoDB() ) {
            log.info("cheduled cron -> runEPGDump()");
            runEPGDump();
        }
    }

    void runEPGDump(){

        log.info("run runEPGDump()");
        Resource resource = new ClassPathResource("epgdump_channel_map.json");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Integer> epgdumpChannelMap = objectMapper.readValue(resource.getFile(), HashMap.class);
            log.info(epgdumpChannelMap.toString());

            EPGDumpRunner epgDumpRunner = new EPGDumpRunner(epgdumpChannelMap);
            Thread thread = new Thread(epgDumpRunner);
            thread.start();

        } catch (IOException e) {
            log.error("invalid epgdump_channel_map.json: {} {}", e.getMessage(), e);
        }
    }

    boolean isEPGDumpExecuted(){
        String[] command = {"/bin/sh", "-c", "ps aux | grep epgdump.sh"};
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            Process process = processBuilder.start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String str = "";
            while((str = bufferedReader.readLine()) != null){
                log.info(str);
                if(str.contains("epgdump.sh") && !str.contains("grep")){
                    bufferedReader.close();
                    process.destroy();
                    return true;
                }
            }
            bufferedReader.close();
            process.destroy();
        } catch (IOException e) {
            log.error("{} {}", e.getMessage(), e);
        }
        return false;
    }
}
