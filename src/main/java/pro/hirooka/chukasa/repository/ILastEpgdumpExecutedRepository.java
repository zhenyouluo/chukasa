package pro.hirooka.chukasa.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pro.hirooka.chukasa.domain.epgdump.LastEpgdumpExecuted;

public interface ILastEpgdumpExecutedRepository extends MongoRepository<LastEpgdumpExecuted, Integer> {
}
