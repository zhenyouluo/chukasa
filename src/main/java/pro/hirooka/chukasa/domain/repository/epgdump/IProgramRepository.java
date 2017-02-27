package pro.hirooka.chukasa.domain.repository.epgdump;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pro.hirooka.chukasa.domain.model.recorder.Program;

import java.util.List;

public interface IProgramRepository extends MongoRepository<Program, String> {

    // ex. {$and:[{'piyo':{$eq:123}},{'channel':{$regex:/^GR_/}}]}
    //@Query("{$and:[{'piyo':{$eq:?0}},{'channel':{$regex:?1}}]}")
    List<Program> findAllByChannel(String channel);
    List<Program> findAllByPhysicalChannel(int physicalChannel);

    @Query("{$and:[{'physicalChannel':{$eq:?0}},{'start':{$lte:?1}},{'end':{$gte:?1}}]}")
    Program findOneByPhysicalChannelAndNowLike(String physicalChannel, long now);

    @Query("{$and:[{'start':{$lte:?0}},{'end':{$gte:?0}},{'physicalChannel':{$ne:0}}]}")
    List<Program> findAllByNowLike(long now);

    @Query("{$and:[{'start':{$lte:?1}},{'end':{$gte:?1}},{'physicalChannel':{$ne:?0}}]}")
    List<Program> findOneByPhysicalChannelAndNowLike(int physicalChannel, long now);

    @Query("{$and:[{'physicalChannel':{$eq:?0}},{'start':{$lte:?2}},{'start':{$gte:?1}}]}")
    Program findOneByPhysicalChannelAndFromAndToLike(int physicalChannel, long from, long to);

    @Query("{$and:[{'begin':{$lte:?0}},{'end':{$lte:?0}}]}")
    List<Program> deleteByDate(long date);

    @Query("{'end':{$lte:?0}}")
    List<Program> deleteByEnd(long end);

    @Query("{$and:[{'begin':{$gte:?0}},{'end':{$lte:?1}},{'physicalChannel':{$ne:0}}]}")
    List<Program> findAllByBeginAndEndLike(long begin, long end);

//    @Query("{$and:[{'begin':{$lte:?0}},{'end':{$lte:?0}}]}")
//    Long deleteProgramByDate(long date); // 機能しない...
}
