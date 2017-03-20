package pro.hirooka.chukasa.domain.service.common.ulitities;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.configuration.ChukasaConfiguration;
import pro.hirooka.chukasa.domain.model.recorder.ChannelConfiguration;
import pro.hirooka.chukasa.domain.model.recorder.ChannelConfigurationWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static pro.hirooka.chukasa.domain.model.chukasa.constants.ChukasaConstant.FILE_SEPARATOR;
import static pro.hirooka.chukasa.domain.model.chukasa.constants.ChukasaConstant.STREAM_ROOT_PATH_NAME;

@Slf4j
@Component
public class CommonUtilityService implements ICommonUtilityService {

    @Autowired
    private ChukasaConfiguration chukasaConfiguration;

    @Override
    public List<ChannelConfiguration> getChannelConfigurationList() {
        List<ChannelConfiguration> channelConfigurationList = new ArrayList<>();
        try {
            Resource resource = new ClassPathResource(chukasaConfiguration.getChannelConfiguration());
            ObjectMapper objectMapper = new ObjectMapper();
            channelConfigurationList = objectMapper.readValue(resource.getInputStream(), ChannelConfigurationWrapper.class).getChannelConfigurationList();
            log.info(channelConfigurationList.toString());
        } catch (IOException e) {
            log.error("invalid channel_settings.json: {} {}", e.getMessage(), e);
        }
        return channelConfigurationList;
    }

    @Override
    public String getStreamRootPath(String servletRealPath) {
        if(servletRealPath.substring(servletRealPath.length() - 1).equals(FILE_SEPARATOR)) {
            return servletRealPath + STREAM_ROOT_PATH_NAME; // e.g. Tomcat
        } else {
            return servletRealPath + FILE_SEPARATOR + STREAM_ROOT_PATH_NAME; // e.g. Jetty
        }
    }
}
