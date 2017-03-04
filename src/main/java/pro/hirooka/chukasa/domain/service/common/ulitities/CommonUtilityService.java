package pro.hirooka.chukasa.domain.service.common.ulitities;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.configuration.ChukasaConfiguration;
import pro.hirooka.chukasa.domain.model.recorder.ChannelPreferences;
import pro.hirooka.chukasa.domain.model.recorder.ChannelPreferencesWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CommonUtilityService implements ICommonUtilityService {

    @Autowired
    private ChukasaConfiguration chukasaConfiguration;

    @Override
    public List<ChannelPreferences> getChannelPreferencesList() {
        List<ChannelPreferences> channelPreferencesList = new ArrayList<>();
        try {
            Resource resource = new ClassPathResource(chukasaConfiguration.getRecorderChannelPreferences());
            ObjectMapper objectMapper = new ObjectMapper();
            channelPreferencesList = objectMapper.readValue(resource.getInputStream(), ChannelPreferencesWrapper.class).getChannelPreferencesList();
            log.info(channelPreferencesList.toString());
        } catch (IOException e) {
            log.error("invalid channel_preferences.json: {} {}", e.getMessage(), e);
        }
        return channelPreferencesList;
    }
}
