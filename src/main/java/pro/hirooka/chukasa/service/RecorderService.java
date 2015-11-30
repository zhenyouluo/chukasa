package pro.hirooka.chukasa.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.ReservedProgram;
import pro.hirooka.chukasa.recorder.Recorder;
import pro.hirooka.chukasa.recorder.RecorderRunner;
import pro.hirooka.chukasa.repository.IReservedProgramRepository;

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

    @Override
    public ReservedProgram create(ReservedProgram reservedProgram) {

        List<ReservedProgram> reservedProgramList = reservedProgramRepository.findAll();
        reservedProgramList.stream().map(ReservedProgram::getId);
        int n = Collections.max(reservedProgramList.stream().map(ReservedProgram::getId).collect(Collectors.toList()));
        reservedProgram.setId(n++);

        long begin = reservedProgram.getBegin();
        long start = reservedProgram.getStart();
        long end = reservedProgram.getEnd();

        Date date = new Date();
        long now = date.getTime();

        log.info("{}, {}, {}, {}", now, begin, start, end);
        log.info("{}, {}, {}, {}", date, new Date(begin), new Date(start), new Date(end));

        if(start > now){

            // reserve
            Recorder recorder = new Recorder(systemConfiguration);
            recorder.reserve(Arrays.asList(reservedProgram));

        }else if(now > start && end > now){

            // start recording immediately
            RecorderRunner recorderRunner = new RecorderRunner(systemConfiguration, reservedProgram);
            Thread thread = new Thread(recorderRunner);
            thread.start();
            // todo adjust duration

        }else if(now > end){

            //  nothing to do... (as error)

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
