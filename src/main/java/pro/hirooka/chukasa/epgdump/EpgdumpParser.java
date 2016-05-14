package pro.hirooka.chukasa.epgdump;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.recorder.Channel;
import pro.hirooka.chukasa.service.recorder.IProgramTableService;

import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
public class EpgdumpParser implements IEpgdumpParser {

    private final IProgramTableService epgDumpProgramTableService;

    @Autowired
    public EpgdumpParser(IProgramTableService epgDumpProgramTableService){
        this.epgDumpProgramTableService = requireNonNull(epgDumpProgramTableService, "epgDumpProgramTableService");
    }

    @Override
    public void parse(String path, Map<String, Integer> epgdumpChannelMap) {

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(path)))) {
//            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(path)));
//            String jsonString = "";
//            String str = "";
//            while((str = bufferedReader.readLine()) != null){
//                jsonString = str;
//            }
            String jsonString = bufferedReader.readLine();
//            String editedJsonString = jsonString.substring(1, jsonString.split("").length - 1);

            ObjectMapper objectMapper = new ObjectMapper();
//            EPGDumpChannelInformation epgDumpChannelInformation = objectMapper.readValue(editedJsonString, EPGDumpChannelInformation.class);
//            epgDumpChannelInformation.getPrograms().forEach(epgDumpProgramInformation -> {
//                log.info("{}", epgDumpProgramInformation.toString());
//            });

            List<Channel> channelList = objectMapper.readValue(jsonString, new TypeReference<List<Channel>>(){});
            log.info("channel = {}", channelList.size());
            channelList.forEach(channel -> {
                channel.getPrograms().forEach(program -> {
                    log.debug("{}", program.toString());
                    if(epgdumpChannelMap.keySet().contains(program.getChannel())){
                        for(Map.Entry<String, Integer> entry : epgdumpChannelMap.entrySet()) {
                            if(program.getChannel().equals(entry.getKey())){
                                program.setCh(entry.getValue());
                            }
                        }
                    }
                    program.setChannelName(channel.getName());
                    long begin = program.getStart() / 10;
                    long end = program.getEnd() / 10;
                    program.setStart(begin);
                    program.setEnd(end);
                    program.setBeginDate(convertMilliToDate(begin));
                    program.setEndDate(convertMilliToDate(end));
                    epgDumpProgramTableService.create(program);
                });
            });

        } catch (IOException e) {
            log.error("{} {}", e.getMessage(), e);
        }
    }

    String convertMilliToDate(long milli){
        Instant instant = Instant.ofEpochMilli(milli);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        return zonedDateTime.format(dateTimeFormatter);
    }
}
