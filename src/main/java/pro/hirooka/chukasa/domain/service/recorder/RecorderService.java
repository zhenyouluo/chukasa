package pro.hirooka.chukasa.domain.service.recorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.configuration.ChukasaConfiguration;
import pro.hirooka.chukasa.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.model.recorder.RecordingProgramModel;
import pro.hirooka.chukasa.domain.model.recorder.ReservedProgram;
import pro.hirooka.chukasa.domain.repository.recorder.IReservedProgramRepository;
import pro.hirooka.chukasa.domain.service.chukasa.IRecordingProgramManagementComponent;
import pro.hirooka.chukasa.domain.service.chukasa.ISystemService;

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
    @Autowired
    IRecordingProgramManagementComponent recordingProgramManagementComponent;

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
                        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
                        RecorderRunner recorderRunner = new RecorderRunner(systemConfiguration, reservedProgram);
                        simpleAsyncTaskExecutor.execute(recorderRunner);

                        RecordingProgramModel recordingProgramModel = new RecordingProgramModel();
                        recordingProgramModel.setFileName(reservedProgram.getFileName());
                        recordingProgramModel.setStartRecording(reservedProgram.getStartRecording());
                        recordingProgramModel.setStopRecording(reservedProgram.getStopRecording());
                        recordingProgramManagementComponent.create(reservedProgram.getId(), recordingProgramModel);

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

        long startRecording = reservedProgram.getBegin() - chukasaConfiguration.getRecorderStartMargin() * 1000;
        long stopRecording = reservedProgram.getEnd() + chukasaConfiguration.getRecorderStopMargin() * 1000;
        long durationRecording = (stopRecording - startRecording) / 1000;
        long recordingDuration = (stopRecording - startRecording) / 1000;
        reservedProgram.setStartRecording(startRecording);
        reservedProgram.setStopRecording(stopRecording);
        reservedProgram.setDurationRecording(durationRecording);
        reservedProgram.setRecordingDuration(recordingDuration);

        String fileName = reservedProgram.getPhysicalChannel() + "_" + reservedProgram.getBegin() + "_" + reservedProgram.getTitle()  + ".ts";
        reservedProgram.setFileName(fileName);

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
            SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
            RecorderRunner recorderRunner = new RecorderRunner(systemConfiguration, reservedProgram);
            simpleAsyncTaskExecutor.execute(recorderRunner);

            RecordingProgramModel recordingProgramModel = new RecordingProgramModel();
            recordingProgramModel.setFileName(reservedProgram.getFileName());
            recordingProgramModel.setStartRecording(reservedProgram.getStartRecording());
            recordingProgramModel.setStopRecording(reservedProgram.getStopRecording());
            recordingProgramManagementComponent.create(reservedProgram.getId(), recordingProgramModel);

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
