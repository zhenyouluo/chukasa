package pro.hirooka.chukasa.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pro.hirooka.chukasa.domain.LastEPGCrawlerExecuted;

public interface ILastEPGCrawlerExecutedRepository extends MongoRepository<LastEPGCrawlerExecuted, Integer> {
}
