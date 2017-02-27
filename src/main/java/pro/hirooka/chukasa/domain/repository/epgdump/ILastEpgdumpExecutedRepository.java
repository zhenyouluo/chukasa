package pro.hirooka.chukasa.domain.repository.epgdump;

import org.springframework.data.mongodb.repository.MongoRepository;
import pro.hirooka.chukasa.domain.model.epgdump.LastEpgdumpExecuted;

public interface ILastEpgdumpExecutedRepository extends MongoRepository<LastEpgdumpExecuted, Integer> {
}
