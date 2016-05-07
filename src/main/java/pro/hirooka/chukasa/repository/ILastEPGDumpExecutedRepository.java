package pro.hirooka.chukasa.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pro.hirooka.chukasa.domain.LastEPGDumpExecuted;

public interface ILastEPGDumpExecutedRepository extends MongoRepository<LastEPGDumpExecuted, Integer> {
}
