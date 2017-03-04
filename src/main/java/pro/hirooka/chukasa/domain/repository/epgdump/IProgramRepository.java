package pro.hirooka.chukasa.domain.repository.epgdump;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pro.hirooka.chukasa.domain.model.recorder.Program;

import java.util.List;

public interface IProgramRepository extends MongoRepository<Program, String> {

    // ex. {$and:[{'piyo':{$eq:123}},{'channel':{$regex:/^GR_/}}]}
    //@Query("{$and:[{'piyo':{$eq:?0}},{'channel':{$regex:?1}}]}")
    List<Program> findAllByChannel(String channel);
    List<Program> findAllByPhysicalLogicalChannel(int physicalLogicalChannel);

    @Query("{$and:[{'physicalLogicalChannel':{$eq:?0}},{'start':{$lte:?1}},{'end':{$gte:?1}}]}")
    Program findOneByPhysicalLogicalChannelAndNowLike(String physicalLogicalChannel, long now);

    // spring-data-mongodb:1.9.x.RELEASE から spring-data-mongodb:1.10.0.RELEASE にすると機能せず
    @Query("{$and:[{'start':{$lte:?0}},{'end':{$gte:?0}},{'physicalLogicalChannel':{$ne:0}}]}")
    List<Program> findAllByNowLike(long now);

    @Query("{$and:[{'start':{$lte:?1}},{'end':{$gte:?1}},{'physicalLogicalChannel':{$ne:?0}}]}")
    List<Program> findOneByPhysicalLogicalChannelAndNowLike(int physicalLogicalChannel, long now);

    @Query("{$and:[{'physicalLogicalChannel':{$eq:?0}},{'start':{$lte:?2}},{'start':{$gte:?1}}]}")
    Program findOneByPhysicalLogicalChannelAndFromAndToLike(int physicalLogicalChannel, long from, long to);

    @Query("{$and:[{'begin':{$lte:?0}},{'end':{$lte:?0}}]}")
    List<Program> deleteByDate(long date);

    @Query("{'end':{$lte:?0}}")
    List<Program> deleteByEnd(long end);

    @Query("{$and:[{'begin':{$gte:?0}},{'end':{$lte:?1}},{'physicalLogicalChannel':{$ne:0}}]}")
    List<Program> findAllByBeginAndEndLike(long begin, long end);

//    @Query("{$and:[{'begin':{$lte:?0}},{'end':{$lte:?0}}]}")
//    Long deleteProgramByDate(long date); // 機能しない...
}
