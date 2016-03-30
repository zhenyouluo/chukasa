package pro.hirooka.chukasa.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pro.hirooka.chukasa.domain.ProgramInformation;

import java.util.List;

public interface IProgramInformationRepository extends MongoRepository<ProgramInformation, Long> {
    List<ProgramInformation> findAllByCh(int ch);
    List<ProgramInformation> findAllByBeginDateLike(String beginDate);
    // ex. {$and:[{'ch':{$eq:123}},{'beginDate':{$regex:/^201512/}}]}
    @Query("{$and:[{'ch':{$eq:?0}},{'beginDate':{$regex:?1}}]}")
    List<ProgramInformation> findAllByChAndBeginDateLike(int ch, String beginDate);

    List<ProgramInformation> findAllByBeginDate(String beginDate);
}
