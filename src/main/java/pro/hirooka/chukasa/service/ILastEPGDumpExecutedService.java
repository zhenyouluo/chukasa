package pro.hirooka.chukasa.service;

import pro.hirooka.chukasa.domain.LastEPGDumpExecuted;

public interface ILastEPGDumpExecutedService {
    LastEPGDumpExecuted create(LastEPGDumpExecuted lastEPGDumpExecuted);
    LastEPGDumpExecuted read(int unique);
    LastEPGDumpExecuted update(LastEPGDumpExecuted lastEPGDumpExecuted);
    void delete(int unique);
}
