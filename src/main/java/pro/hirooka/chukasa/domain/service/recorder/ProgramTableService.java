package pro.hirooka.chukasa.domain.service.recorder;

import com.mongodb.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.hirooka.chukasa.configuration.MongoDBConfiguration;
import pro.hirooka.chukasa.domain.model.recorder.Program;
import pro.hirooka.chukasa.domain.repository.epgdump.IProgramRepository;
import pro.hirooka.chukasa.domain.service.chukasa.ISystemService;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProgramTableService implements IProgramTableService {

    @Autowired
    private ISystemService systemService;

    @Autowired
    private IProgramRepository programRepository;

    @Autowired
    private MongoDBConfiguration mongoDBConfiguration;

    @PostConstruct
    public void init(){
        deleteOldProgram();
    }

    @Override
    public Program create(Program Program) {
        return programRepository.save(Program);
    }

    @Override
    public List<Program> read() {
        return programRepository.findAll();
    }

    @Override
    public List<Program> read(int physicalLogicalChannel) {
//        return programRepository.findAllByPhysicalChannel(physicalChannel);
        return programRepository.findAllByPhysicalLogicalChannel(physicalLogicalChannel);
    }

    @Override
    public List<Program> readByBeginDate(String beginDate) {
        return null;
    }

    @Override
    public List<Program> read(int ch, String beginDate) {
        return null;
    }

    @Override
    public List<Program> readByNow(long now)  {
        //return programRepository.findAllByNowLike(now); // spring-data-mongodb:1.9.x.RELEASE から spring-data-mongodb:1.10.0.RELEASE にすると機能せず
        MongoTemplate mongoTemplate = new MongoTemplate(new SimpleMongoDbFactory(new MongoClient(mongoDBConfiguration.getHost(), mongoDBConfiguration.getPort()), mongoDBConfiguration.getDatabase()));
        Query query = new Query(Criteria.where("start").lte(now).and("end").gte(now)).with(new Sort(Sort.Direction.ASC, "remoteControllerChannel"));
        return mongoTemplate.find(query, Program.class);
    }

    @Override
    public List<Program> readOneDayByNow(long now) {
        Instant instant = Instant.ofEpochMilli(now);
        ZonedDateTime nowZonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        ZonedDateTime tomorrowZonedDateTime = ZonedDateTime.from(instant.atZone(ZoneId.systemDefault())).plusDays(1);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        String nowZonedDateTimeString = nowZonedDateTime.format(dateTimeFormatter);
        String tomorrowZonedDateTimeString = tomorrowZonedDateTime.format(dateTimeFormatter);
        log.info("nowZonedDateTime = {}, {}", nowZonedDateTimeString, nowZonedDateTime.toEpochSecond());
        log.info("tomorrowZonedDateTime = {}, {}", tomorrowZonedDateTimeString, tomorrowZonedDateTime.toEpochSecond());

        //return programRepository.findAllByBeginAndEndLike(now, tomorrowZonedDateTime.toEpochSecond() * 1000);
        MongoTemplate mongoTemplate = new MongoTemplate(new SimpleMongoDbFactory(new MongoClient(mongoDBConfiguration.getHost(), mongoDBConfiguration.getPort()), mongoDBConfiguration.getDatabase()));
        Query query = new Query(Criteria.where("begin").lte(now).and("end").lte(tomorrowZonedDateTime.toEpochSecond() * 1000)).with(new Sort(Sort.Direction.ASC, "remoteControllerChannel"));
        return mongoTemplate.find(query, Program.class);
    }

    @Override
    public Program read(String id) {
        return programRepository.findOne(id);
    }

    @Override
    public Program readNow(int ch, long now) {
        return null;
    }

    @Override
    public Program update(Program Program) {
        return programRepository.save(Program);
    }

    @Override
    public void delete(String id) {
        programRepository.delete(id);
    }

    @Override
    public void deleteAll() {
        programRepository.deleteAll();
    }

    @Override
    public int getNumberOfPhysicalLogicalChannels() {
        return programRepository.findAll().stream().map(Program::getChannel).collect(Collectors.toSet()).size();
    }

    @Scheduled(cron = "0 0 6 * * *")
    void execute(){
        deleteOldProgram();
    }

    void deleteOldProgram(){

        if(systemService.isMongoDB()) {
            Date date = new Date();
            Instant instant = Instant.ofEpochMilli(date.getTime());
            ZonedDateTime zonedDateTime = ZonedDateTime.from(instant.atZone(ZoneId.systemDefault())).minusDays(1);
            int year = zonedDateTime.getYear();
            int month = zonedDateTime.getMonthValue();
            int day = zonedDateTime.getDayOfMonth();
            ZonedDateTime thresholdZonedDateTime = ZonedDateTime.of(year, month, day, 0, 0, 0, 0, ZoneId.systemDefault());

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
            String thresholdZonedDateTimeString = thresholdZonedDateTime.format(dateTimeFormatter);
            log.info("thresholdZonedDateTime = {}, {}", thresholdZonedDateTimeString, thresholdZonedDateTime.toEpochSecond());

            List<Program> toBeDeletedProgramList = programRepository.deleteByEnd(thresholdZonedDateTime.toEpochSecond() * 1000);
            log.info("toBeDeletedProgramList.size() = {}", toBeDeletedProgramList.size());
            toBeDeletedProgramList.forEach(program -> programRepository.delete(program.getId()));
        }
    }
}
