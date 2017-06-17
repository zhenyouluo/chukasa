package pro.hirooka.chukasa.domain.service.epgdump;

import com.mongodb.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.configuration.MongoDBConfiguration;
import pro.hirooka.chukasa.domain.model.epgdump.LastEpgdumpExecuted;
import pro.hirooka.chukasa.domain.repository.epgdump.ILastEpgdumpExecutedRepository;

import java.util.List;

@Slf4j
@Component
public class LastEpgdumpExecutedService implements ILastEpgdumpExecutedService {

    @Autowired
    ILastEpgdumpExecutedRepository lastEpgdumpExecutedRepository;
    @Autowired
    private MongoDBConfiguration mongoDBConfiguration;

    @Override
    public LastEpgdumpExecuted create(LastEpgdumpExecuted lastEPGDumpExecuted) {
        return lastEpgdumpExecutedRepository.save(lastEPGDumpExecuted);
    }

    @Override
    public LastEpgdumpExecuted read(int unique) {

        MongoTemplate mongoTemplate = new MongoTemplate(new SimpleMongoDbFactory(new MongoClient(mongoDBConfiguration.getHost(), mongoDBConfiguration.getPort()), mongoDBConfiguration.getDatabase()));
        //Query query = new Query(Criteria.where("unique").is(unique)).with(new Sort(Sort.Direction.ASC,"unique"));
        List<LastEpgdumpExecuted> lastEpgdumpExecutedList = mongoTemplate.findAll(LastEpgdumpExecuted.class);
        if(lastEpgdumpExecutedList.size() != 1){
            log.error("e");
            return null;
        }else{
            return lastEpgdumpExecutedList.get(0);
        }
    }

    @Override
    public LastEpgdumpExecuted update(LastEpgdumpExecuted lastEpgdumpExecuted) {
        return lastEpgdumpExecutedRepository.save(lastEpgdumpExecuted);
    }

    @Override
    public void delete(int unique) {
        lastEpgdumpExecutedRepository.deleteById(unique);
    }
}
