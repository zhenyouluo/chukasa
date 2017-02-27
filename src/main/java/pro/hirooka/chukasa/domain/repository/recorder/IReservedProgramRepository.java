package pro.hirooka.chukasa.domain.repository.recorder;

import org.springframework.data.mongodb.repository.MongoRepository;
import pro.hirooka.chukasa.domain.model.recorder.ReservedProgram;

public interface IReservedProgramRepository extends MongoRepository<ReservedProgram, Integer> {
}
