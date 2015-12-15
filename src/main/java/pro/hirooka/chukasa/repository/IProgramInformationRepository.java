package pro.hirooka.chukasa.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pro.hirooka.chukasa.domain.ProgramInformation;

public interface IProgramInformationRepository extends MongoRepository<ProgramInformation, Long> {
}
