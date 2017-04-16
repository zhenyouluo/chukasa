package pro.hirooka.chukasa.domain.model.recorder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import pro.hirooka.chukasa.domain.model.recorder.enums.ChannelType;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ChannelConfiguration { // TODO: -> common
    private ChannelType channelType;
    private int remoteControllerChannel;
    private int physicalLogicalChannel;
    private int frequency;
}
