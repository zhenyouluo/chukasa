package pro.hirooka.chukasa.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pro.hirooka.chukasa.domain.ReservedProgram;

public interface IReservedProgramRepository extends MongoRepository<ReservedProgram, Integer> {
}
