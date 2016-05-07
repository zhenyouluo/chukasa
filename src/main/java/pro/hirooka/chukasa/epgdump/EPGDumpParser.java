package pro.hirooka.chukasa.epgdump;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.EPGDumpChannelInformation;

import java.io.*;

@Slf4j
@Component
public class EPGDumpParser implements IEPGDumpParser {

    @Override
    public void parse(String path) {

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(path)));
//            String jsonString = "";
//            String str = "";
//            while((str = bufferedReader.readLine()) != null){
//                jsonString = str;
//            }
            String jsonString = bufferedReader.readLine();
            String editedJsonString = jsonString.substring(1, jsonString.split("").length - 1);

            ObjectMapper objectMapper = new ObjectMapper();
            EPGDumpChannelInformation epgDumpChannelInformation = objectMapper.readValue(editedJsonString, EPGDumpChannelInformation.class);

//            List<EPGDumpChannelInformation> epgDumpChannelInformationList = new ObjectMapper().readValue(jsonString, new TypeReference<List<EPGDumpChannelInformation>>(){});

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
