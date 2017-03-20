package pro.hirooka.chukasa.domain.service.epgdump.runner;

import pro.hirooka.chukasa.domain.model.recorder.ChannelConfiguration;

import java.util.List;
import java.util.concurrent.Future;

public interface IEpgdumpRunnerService {
    Future<Integer> submit(List<ChannelConfiguration> channelConfigurationList);
    void cancel();
}
