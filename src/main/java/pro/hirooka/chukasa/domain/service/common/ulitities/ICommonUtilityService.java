package pro.hirooka.chukasa.domain.service.common.ulitities;

import pro.hirooka.chukasa.domain.model.common.Tuner;
import pro.hirooka.chukasa.domain.model.recorder.ChannelConfiguration;

import java.util.List;

public interface ICommonUtilityService {
    List<ChannelConfiguration> getChannelConfigurationList();
    String getStreamRootPath(String servletRealPath);
    List<Tuner> getTunerList();
}
