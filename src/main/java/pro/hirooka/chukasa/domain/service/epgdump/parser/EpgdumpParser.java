package pro.hirooka.chukasa.domain.service.epgdump.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.model.recorder.Channel;
import pro.hirooka.chukasa.domain.service.recorder.IProgramTableService;

import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class EpgdumpParser implements IEpgdumpParser {

    @Autowired
    IProgramTableService epgDumpProgramTableService;

    @Override
    public void parse(String path, int physicalChannel, Map<String, String> epgdumpChannelMap) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(path)));
        String jsonString = bufferedReader.readLine();

        ObjectMapper objectMapper = new ObjectMapper();

        List<Channel> channelList = objectMapper.readValue(jsonString, new TypeReference<List<Channel>>(){});
        log.info("channel = {}", channelList.size());
        channelList.forEach(channel -> {
            channel.getPrograms().forEach(program -> {
                log.debug("{}", program.toString());
                if(program.getChannel().startsWith("GR")) {
                    program.setId(physicalChannel + "_" + program.getStart());
                    program.setPhysicalChannel(physicalChannel);
                    program.setChannelName(channel.getName());
                    long begin = program.getStart() / 10;
                    long end = program.getEnd() / 10;
                    program.setBegin(begin);
                    program.setStart(begin);
                    program.setEnd(end);
                    program.setBeginDate(convertMilliToDate(begin));
                    program.setEndDate(convertMilliToDate(end));
                    epgDumpProgramTableService.create(program);
                }else if(program.getChannel().startsWith("BS_")){
                    try {
                        int physicalChannelBS = Integer.parseInt(program.getChannel().split("BS_")[1]);
                        program.setId(physicalChannelBS + "_" + program.getStart());
                        program.setPhysicalChannel(physicalChannelBS);
                        program.setChannelName(channel.getName());
                        long begin = program.getStart() / 10;
                        long end = program.getEnd() / 10;
                        program.setBegin(begin);
                        program.setStart(begin);
                        program.setEnd(end);
                        program.setBeginDate(convertMilliToDate(begin));
                        program.setEndDate(convertMilliToDate(end));
                        epgDumpProgramTableService.create(program);
                    }catch(NumberFormatException e){
                        log.error("invalid channel", e.getMessage(), e);
                    }
                }else{
                    log.info("program.getChannel is not GR|BS.");
                }
            });
        });
    }

    @Override
    public void parse(String path, Map<String, Integer> epgdumpChannelMap) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(path)));
        String jsonString = bufferedReader.readLine();

        ObjectMapper objectMapper = new ObjectMapper();

        List<Channel> channelList = objectMapper.readValue(jsonString, new TypeReference<List<Channel>>(){});
        log.info("channel = {}", channelList.size());
        channelList.forEach(channel -> {
            channel.getPrograms().forEach(program -> {
                log.debug("{}", program.toString());
                if(epgdumpChannelMap.keySet().contains(program.getChannel())){
                    for(Map.Entry<String, Integer> entry : epgdumpChannelMap.entrySet()) {
                        if(program.getChannel().equals(entry.getKey())){
                            program.setPhysicalChannel(entry.getValue());
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
    }

    String convertMilliToDate(long milli){
        Instant instant = Instant.ofEpochMilli(milli);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        return zonedDateTime.format(dateTimeFormatter);
    }
}
