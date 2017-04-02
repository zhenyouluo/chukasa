package pro.hirooka.chukasa.domain.model.common;

import lombok.Data;
import pro.hirooka.chukasa.domain.model.recorder.enums.ChannelType;

@Data
public class Tuner {
    private ChannelType channelType;
    private String deviceName;
}
