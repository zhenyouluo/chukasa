package pro.hirooka.chukasa.domain.repository.epgdump;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pro.hirooka.chukasa.domain.model.epgdump.LastEpgdumpExecuted;

@Repository
public interface ILastEpgdumpExecutedRepository extends MongoRepository<LastEpgdumpExecuted, Integer> {
}
