package pro.hirooka.chukasa.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pro.hirooka.chukasa.domain.recorder.Program;

import java.util.List;

public interface IProgramRepository extends MongoRepository<Program, Long> {

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
}
