package pro.hirooka.chukasa.service.recorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.configuration.ChukasaConfiguration;
import pro.hirooka.chukasa.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.recorder.ReservedProgram;
import pro.hirooka.chukasa.recorder.*;
import pro.hirooka.chukasa.repository.IReservedProgramRepository;
import pro.hirooka.chukasa.service.system.ISystemService;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RecorderService implements IRecorderService {

    @Autowired
    SystemConfiguration systemConfiguration;
    @Autowired
    ChukasaConfiguration chukasaConfiguration;
    @Autowired
    IReservedProgramRepository reservedProgramRepository;
    @Autowired
    ISystemService systemService;

    @PostConstruct
    public void init(){

        if(systemService.isPTx() && systemService.isRecpt1() && systemService.isFFmpeg() && systemService.isEpgdump() && systemService.isMongoDB()){
            List<ReservedProgram> reservedProgramList = read();
            for(ReservedProgram reservedProgram : reservedProgramList){
                if(true){ // TODO: checker

                    long startRecording = reservedProgram.getStartRecording();
                    long stopRecording = reservedProgram.getStopRecording();
                    long now = new Date().getTime();
                    if (startRecording > now && stopRecording > now) {

                        // reserve
                        log.info("reservation: {}", reservedProgram.toString());

                        Recorder recorder = new Recorder();
                        recorder.reserve(reservedProgram);

                    } else if (now > startRecording && stopRecording > now) {

                        // start recording immediately
                        log.info("no reservation, direct recording");

                        long duration = (stopRecording - now) / 1000;
                        reservedProgram.setDuration(duration);
                        RecorderRunner recorderRunner = new RecorderRunner(reservedProgram);
                        Thread thread = new Thread(recorderRunner);
                        thread.start();

                    } else if (now > startRecording && now > stopRecording) {

                        //  nothing to do... (as error)
                        log.info("no reservation, no recording");

                    } else {
                        // todo
                    }

                }else{
                    log.info("skip (in recording...) {}", reservedProgram.toString());
                }
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

        long startRecording = reservedProgram.getStart() - chukasaConfiguration.getRecorderStartMargin() * 1000;
        long stopRecording = reservedProgram.getEnd() + chukasaConfiguration.getRecorderStopMargin() * 1000;
        long durationRecording = (stopRecording - startRecording) / 1000;
        reservedProgram.setStartRecording(startRecording);
        reservedProgram.setStopRecording(stopRecording);
        reservedProgram.setDurationRecording(durationRecording);

        long now = new Date().getTime();

        if(startRecording > now && stopRecording > now){

            // reserve
            log.info("reservation");

            Recorder recorder = new Recorder();
            recorder.reserve(reservedProgram);

        }else if(now > startRecording && stopRecording > now){

            // start recording immediately
            log.info("no reservation, direct recording");

            durationRecording = (stopRecording - now) / 1000;
            reservedProgram.setDurationRecording(durationRecording);
            RecorderRunner recorderRunner = new RecorderRunner(reservedProgram);
            Thread thread = new Thread(recorderRunner);
            thread.start();

        }else if(now > startRecording && now > stopRecording){

            //  nothing to do... (as error)
            log.info("no reservation, no recording");

        }else{
            //
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
