package pro.hirooka.chukasa.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pro.hirooka.chukasa.domain.EPGDumpReservedProgram;

public interface IEPGDumpReservedProgramRepository extends MongoRepository<EPGDumpReservedProgram, Integer> {
}
