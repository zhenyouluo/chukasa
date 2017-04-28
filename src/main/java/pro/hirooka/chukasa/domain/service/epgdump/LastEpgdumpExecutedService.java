package pro.hirooka.chukasa.domain.service.epgdump;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.model.epgdump.LastEpgdumpExecuted;
import pro.hirooka.chukasa.domain.repository.epgdump.ILastEpgdumpExecutedRepository;

@Slf4j
@Component
public class LastEpgdumpExecutedService implements ILastEpgdumpExecutedService {

    @Autowired
    ILastEpgdumpExecutedRepository lastEpgdumpExecutedRepository;

    @Override
    public LastEpgdumpExecuted create(LastEpgdumpExecuted lastEPGDumpExecuted) {
        return lastEpgdumpExecutedRepository.save(lastEPGDumpExecuted);
    }

    @Override
    public LastEpgdumpExecuted read(int unique) {
        return lastEpgdumpExecutedRepository.findOne(unique).orElse(null);
    }

    @Override
    public LastEpgdumpExecuted update(LastEpgdumpExecuted lastEpgdumpExecuted) {
        return lastEpgdumpExecutedRepository.save(lastEpgdumpExecuted);
    }

    @Override
    public void delete(int unique) {
        lastEpgdumpExecutedRepository.delete(unique);
    }
}
