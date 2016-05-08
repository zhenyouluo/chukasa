package pro.hirooka.chukasa.epgdump;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.EPGDumpChannelInformation;
import pro.hirooka.chukasa.service.IEPGDumpProgramTableService;

import java.io.*;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
public class EPGDumpParser implements IEPGDumpParser {

    private final IEPGDumpProgramTableService epgDumpProgramTableService;

    @Autowired
    public EPGDumpParser(IEPGDumpProgramTableService epgDumpProgramTableService){
        this.epgDumpProgramTableService = requireNonNull(epgDumpProgramTableService, "epgDumpProgramTableService");
    }

    @Override
    public void parse(String path, Map<String, Integer> epgDumpChannelMap) {

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(path)));
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

            List<EPGDumpChannelInformation> epgDumpChannelInformationList = objectMapper.readValue(jsonString, new TypeReference<List<EPGDumpChannelInformation>>(){});
            epgDumpChannelInformationList.forEach(epgDumpChannelInformation -> {
                epgDumpChannelInformation.getPrograms().forEach(epgDumpProgramInformation -> {
                    log.debug("{}", epgDumpProgramInformation.toString());
                    if(epgDumpChannelMap.keySet().contains(epgDumpProgramInformation.getChannel())){
                        for(Map.Entry<String, Integer> entry : epgDumpChannelMap.entrySet()) {
                            if(epgDumpProgramInformation.getChannel().equals(entry.getKey())){
                                epgDumpProgramInformation.setCh(entry.getValue());
                            }
                        }
                    }
                    epgDumpProgramInformation.setChannelName(epgDumpChannelInformation.getName());
                    epgDumpProgramTableService.create(epgDumpProgramInformation);
                });
            });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
