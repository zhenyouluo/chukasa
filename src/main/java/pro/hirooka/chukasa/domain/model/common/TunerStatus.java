package pro.hirooka.chukasa.domain.model.common;

import lombok.Data;
import pro.hirooka.chukasa.domain.model.common.enums.RecxxxDriverType;
import pro.hirooka.chukasa.domain.model.recorder.enums.ChannelType;

@Data
public class TunerStatus {
    private ChannelType channelType;
    private String deviceName;
    private int index;
    boolean canUse;
    private RecxxxDriverType recxxxDriverType;
}
