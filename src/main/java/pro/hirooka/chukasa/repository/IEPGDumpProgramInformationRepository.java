package pro.hirooka.chukasa.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pro.hirooka.chukasa.domain.EPGDumpProgramInformation;

import java.util.List;

public interface IEPGDumpProgramInformationRepository extends MongoRepository<EPGDumpProgramInformation, Long> {
    List<EPGDumpProgramInformation> findAllByChannel(String channel);

    @Query("{$and:[{'channel':{$eq:?0}},{'start':{$lte:?1}},{'end':{$gte:?1}}]}")
    EPGDumpProgramInformation findOneByChannelAndNowLike(String channel, long now);

    @Query("{$and:[{'start':{$lte:?0}},{'end':{$gte:?0}},{'ch':{$ne:0}}]}")
    List<EPGDumpProgramInformation> findAllByNowLike(long now);

    @Query("{$and:[{'channel':{$eq:?0}},{'start':{$lte:?2}},{'start':{$gte:?1}}]}")
    EPGDumpProgramInformation findAllByChannelAndFromAndToLike(String channel, long from, long to);
}
