package pro.hirooka.chukasa.domain.service.common;

import pro.hirooka.chukasa.domain.model.common.TunerStatus;
import pro.hirooka.chukasa.domain.model.common.enums.RecxxxDriverType;
import pro.hirooka.chukasa.domain.model.recorder.enums.ChannelType;

import java.util.List;

public interface ITunerManagementService {
    TunerStatus create(TunerStatus tunerStatus);
    List<TunerStatus> get();
    List<TunerStatus> get(ChannelType channelType);
    List<TunerStatus> available(ChannelType channelType);
    TunerStatus findOne(ChannelType channelType);
    TunerStatus get(String deviceName);
    TunerStatus update(TunerStatus tunerStatus);
    TunerStatus update(TunerStatus tunerStatus, boolean canUse);
    RecxxxDriverType getRecxxxDriverType();
}
