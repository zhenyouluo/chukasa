package pro.hirooka.chukasa.domain.model.recorder;

import lombok.Data;
import pro.hirooka.chukasa.domain.model.recorder.enums.ChannelType;

@Data
public class ChannelConfiguration {
    private ChannelType channelType;
    private int remoteControllerChannel;
    private int physicalLogicalChannel;
}
