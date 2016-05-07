package pro.hirooka.chukasa.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.LastEPGDumpExecuted;
import pro.hirooka.chukasa.repository.ILastEPGDumpExecutedRepository;

@Slf4j
@Component
public class LastEPGDumpExecutedService implements ILastEPGDumpExecutedService {

    @Autowired
    private ILastEPGDumpExecutedRepository lastEPGDumpExecutedRepository;

    @Override
    public LastEPGDumpExecuted create(LastEPGDumpExecuted lastEPGDumpExecuted) {
        return lastEPGDumpExecutedRepository.save(lastEPGDumpExecuted);
    }

    @Override
    public LastEPGDumpExecuted read(int unique) {
        return lastEPGDumpExecutedRepository.findOne(unique);
    }

    @Override
    public LastEPGDumpExecuted update(LastEPGDumpExecuted lastEPGDumpExecuted) {
        return lastEPGDumpExecutedRepository.save(lastEPGDumpExecuted);
    }

    @Override
    public void delete(int unique) {
        lastEPGDumpExecutedRepository.delete(unique);
    }
}
