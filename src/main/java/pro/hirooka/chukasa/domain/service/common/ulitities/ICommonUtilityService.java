package pro.hirooka.chukasa.domain.service.common.ulitities;

import pro.hirooka.chukasa.domain.model.common.Tuner;
import pro.hirooka.chukasa.domain.model.recorder.ChannelConfiguration;
import pro.hirooka.chukasa.domain.model.recorder.enums.ChannelType;

import java.util.List;

public interface ICommonUtilityService {
    List<ChannelConfiguration> getChannelConfigurationList();
    String getStreamRootPath(String servletRealPath);
    List<Tuner> getTunerList();
    ChannelType getChannelType(int physicalLogicalChannel);
}
