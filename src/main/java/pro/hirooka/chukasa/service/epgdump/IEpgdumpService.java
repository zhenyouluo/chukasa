package pro.hirooka.chukasa.service.epgdump;

import pro.hirooka.chukasa.domain.epgdump.EpgdumpStatus;

public interface IEpgdumpService {
    EpgdumpStatus getStatus();
}
