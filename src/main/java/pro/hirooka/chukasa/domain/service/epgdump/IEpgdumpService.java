package pro.hirooka.chukasa.domain.service.epgdump;

import pro.hirooka.chukasa.domain.model.epgdump.enums.EpgdumpStatus;

public interface IEpgdumpService {
    EpgdumpStatus getStatus();
}
