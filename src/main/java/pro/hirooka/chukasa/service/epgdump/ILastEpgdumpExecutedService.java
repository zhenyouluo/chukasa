package pro.hirooka.chukasa.service.epgdump;

import pro.hirooka.chukasa.domain.epgdump.LastEpgdumpExecuted;

public interface ILastEpgdumpExecutedService {
    LastEpgdumpExecuted create(LastEpgdumpExecuted lastEPGDumpExecuted);
    LastEpgdumpExecuted read(int unique);
    LastEpgdumpExecuted update(LastEpgdumpExecuted lastEPGDumpExecuted);
    void delete(int unique);
}
