package pro.hirooka.chukasa.domain.service.epgdump;

import pro.hirooka.chukasa.domain.model.epgdump.LastEpgdumpExecuted;

public interface ILastEpgdumpExecutedService {
    LastEpgdumpExecuted create(LastEpgdumpExecuted lastEPGDumpExecuted);
    LastEpgdumpExecuted read(int unique);
    LastEpgdumpExecuted update(LastEpgdumpExecuted lastEPGDumpExecuted);
    void delete(int unique);
}
