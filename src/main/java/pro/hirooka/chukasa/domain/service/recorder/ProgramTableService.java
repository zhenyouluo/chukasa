package pro.hirooka.chukasa.domain.service.recorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.hirooka.chukasa.domain.model.recorder.Program;
import pro.hirooka.chukasa.domain.repository.epgdump.IProgramRepository;
import pro.hirooka.chukasa.domain.service.chukasa.ISystemService;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
    public List<Program> read(int physicalChannel) {
        return programRepository.findAllByPhysicalChannel(physicalChannel);
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
        return programRepository.findAllByNowLike(now);
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

        return programRepository.findAllByBeginAndEndLike(now, tomorrowZonedDateTime.toEpochSecond() * 1000);
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
    public int getNumberOfPhysicalChannels() {
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
