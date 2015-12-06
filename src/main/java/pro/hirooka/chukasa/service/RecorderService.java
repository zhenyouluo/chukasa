package pro.hirooka.chukasa.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.ReservedProgram;
import pro.hirooka.chukasa.recorder.Recorder;
import pro.hirooka.chukasa.recorder.RecorderRunner;
import pro.hirooka.chukasa.repository.IReservedProgramRepository;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
public class RecorderService implements IRecorderService {

    private final SystemConfiguration systemConfiguration;
    private final IReservedProgramRepository reservedProgramRepository;

    @Autowired
    public RecorderService(SystemConfiguration systemConfiguration, IReservedProgramRepository reservedProgramRepository){
        this.systemConfiguration = requireNonNull(systemConfiguration, "systemConfiguration");
        this.reservedProgramRepository = requireNonNull(reservedProgramRepository, "reservedProgramRepository");
    }

    @PostConstruct
    public void init(){
        List<ReservedProgram> reservedProgramList = read();
        for(ReservedProgram reservedProgram : reservedProgramList){
            long begin = reservedProgram.getBegin();
            long start = reservedProgram.getStart();
            long end = reservedProgram.getEnd();
            long stop = reservedProgram.getStop();
            Date date = new Date();
            long now = date.getTime();
            if(start > now && stop > now){

                // reserve
                log.info("reservation: {}", reservedProgram.toString());

                Recorder recorder = new Recorder(systemConfiguration);
                recorder.reserve(reservedProgram);

            }else if(now > start && stop > now){

                // start recording immediately
                log.info("no reservation, direct recording");

                long duration = stop - now;
                reservedProgram.setDuration(duration);
                RecorderRunner recorderRunner = new RecorderRunner(systemConfiguration, reservedProgram);
                Thread thread = new Thread(recorderRunner);
                thread.start();

            }else if(now > start && now > stop){

                //  nothing to do... (as error)
                log.info("no reservation, no recording");

            }else{
                // todo
            }
        }
    }

    @Override
    public ReservedProgram create(ReservedProgram reservedProgram) {

        List<ReservedProgram> reservedProgramList = reservedProgramRepository.findAll();
        if(reservedProgramList.size() > 0) {
            int n = Collections.max(reservedProgramList.stream().map(ReservedProgram::getId).collect(Collectors.toList()));
            n++;
            reservedProgram.setId(n);
        }else{
            reservedProgram.setId(0);
        }

        long begin = reservedProgram.getBegin();
        long start = reservedProgram.getStart();
        long end = reservedProgram.getEnd();
        long stop = reservedProgram.getStop();

        Date date = new Date();
        long now = date.getTime();

        log.info("now: {}, begin: {}, start: {}, end: {}, stop: {}", now, begin, start, end, stop);
        log.info("now: {}, begin: {}, start: {}, end: {}, stop: {}", date, new Date(begin), new Date(start), new Date(end), new Date(stop));

        if(start > now && stop > now){

            // reserve
            log.info("reservation");

            Recorder recorder = new Recorder(systemConfiguration);
            recorder.reserve(reservedProgram);

        }else if(now > start && stop > now){

            // start recording immediately
            log.info("no reservation, direct recording");

            long duration = stop - now;
            reservedProgram.setDuration(duration);
            RecorderRunner recorderRunner = new RecorderRunner(systemConfiguration, reservedProgram);
            Thread thread = new Thread(recorderRunner);
            thread.start();

        }else if(now > start && now > stop){

            //  nothing to do... (as error)
            log.info("no reservation, no recording");

        }else{
            // todo
        }

        return reservedProgramRepository.save(reservedProgram);
    }

    @Override
    public List<ReservedProgram> read() {
        return reservedProgramRepository.findAll();
    }

    @Override
    public ReservedProgram read(int id) {
        return reservedProgramRepository.findOne(id);
    }

    @Override
    public ReservedProgram update(ReservedProgram reservedProgram) {
        return reservedProgramRepository.save(reservedProgram);
    }

    @Override
    public void delete(int id) {
        reservedProgramRepository.delete(id);
    }

    @Override
    public void deleteAll() {
        reservedProgramRepository.deleteAll();
    }
}
