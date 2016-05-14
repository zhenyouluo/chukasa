package pro.hirooka.chukasa.service.epgdump;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.epgdump.LastEpgdumpExecuted;
import pro.hirooka.chukasa.repository.ILastEpgdumpExecutedRepository;
import pro.hirooka.chukasa.service.epgdump.ILastEpgdumpExecutedService;

@Slf4j
@Component
public class LastEpgdumpExecutedService implements ILastEpgdumpExecutedService {

    @Autowired
    private ILastEpgdumpExecutedRepository lastEpgdumpExecutedRepository;

    @Override
    public LastEpgdumpExecuted create(LastEpgdumpExecuted lastEPGDumpExecuted) {
        return lastEpgdumpExecutedRepository.save(lastEPGDumpExecuted);
    }

    @Override
    public LastEpgdumpExecuted read(int unique) {
        return lastEpgdumpExecutedRepository.findOne(unique);
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
