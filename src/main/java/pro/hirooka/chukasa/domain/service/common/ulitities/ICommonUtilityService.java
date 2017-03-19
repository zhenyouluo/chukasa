package pro.hirooka.chukasa.domain.service.common.ulitities;

import pro.hirooka.chukasa.domain.model.recorder.ChannelSettings;

import java.util.List;

public interface ICommonUtilityService {
    List<ChannelSettings> getChannelSettingsList();
    String getStreamRootPath(String servletRealPath);
}
