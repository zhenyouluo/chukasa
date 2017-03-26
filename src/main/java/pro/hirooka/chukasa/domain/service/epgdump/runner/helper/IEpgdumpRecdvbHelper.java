package pro.hirooka.chukasa.domain.service.epgdump.runner.helper;

import pro.hirooka.chukasa.domain.model.epgdump.RecdvbBSModel;

public interface IEpgdumpRecdvbHelper {
    RecdvbBSModel resovle(int logicalChannel);
}
