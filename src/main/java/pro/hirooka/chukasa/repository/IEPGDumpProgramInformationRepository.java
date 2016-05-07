package pro.hirooka.chukasa.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pro.hirooka.chukasa.domain.EPGDumpProgramInformation;

public interface IEPGDumpProgramInformationRepository extends MongoRepository<EPGDumpProgramInformation, Long> {
}
