package pro.hirooka.chukasa.domain.service.recorder.runner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import pro.hirooka.chukasa.domain.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.model.chukasa.enums.HardwareAccelerationType;
import pro.hirooka.chukasa.domain.model.common.TunerStatus;
import pro.hirooka.chukasa.domain.model.common.enums.TunerUseType;
import pro.hirooka.chukasa.domain.model.recorder.ChannelConfiguration;
import pro.hirooka.chukasa.domain.model.recorder.ReservedProgram;
import pro.hirooka.chukasa.domain.model.recorder.enums.ChannelType;
import pro.hirooka.chukasa.domain.service.chukasa.ISystemService;
import pro.hirooka.chukasa.domain.service.common.ITunerManagementService;
import pro.hirooka.chukasa.domain.service.common.ulitities.ICommonUtilityService;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import static pro.hirooka.chukasa.domain.model.chukasa.constants.ChukasaConstant.FILE_SEPARATOR;

@Slf4j
@EnableAsync
@Service
public class RecorderRunnerService implements IRecorderRunnerService {

    @Autowired
    private SystemConfiguration systemConfiguration;
    @Autowired
    private ICommonUtilityService commonUtilityService;
    @Autowired
    private ITunerManagementService tunerManagementService;
    @Autowired
    private ISystemService systemService;

    @Async
    @Override
    public Future<Integer> submit(ReservedProgram reservedProgram) {

        final int physicalLogicalChannel = reservedProgram.getPhysicalLogicalChannel();
        final long startRecording = reservedProgram.getStartRecording();
        final long stopRecording = reservedProgram.getStopRecording();
        final long duration = reservedProgram.getRecordingDuration();
        final long thumbnailPoint = duration / 3;
        final String title = reservedProgram.getTitle();
        final String fileName = reservedProgram.getFileName();

        log.info("start recording... [{}] {}", physicalLogicalChannel, title);

        final long now = new Date().getTime();

        // start recording immediately
        // Create do-record.sh (do-record_ch_yyyyMMdd_yyyyMMdd.sh)
        final String doRecordFileName = "do-record_" + physicalLogicalChannel + "_" + startRecording + "_" + stopRecording + ".sh";
        final List<ChannelConfiguration> channelConfigurationList = commonUtilityService.getChannelConfigurationList();
        final ChannelType channelType = commonUtilityService.getChannelType(physicalLogicalChannel);
        TunerStatus tunerStatus = tunerManagementService.findOne(channelType);
        if(tunerStatus != null) {
            tunerStatus = tunerManagementService.update(tunerStatus, false);
        }else{
            // TODO: priority
            log.warn("Tuner for recording is not available.");
            return new AsyncResult<>(-1);
        }
        final String DEVICE_OPTION = tunerManagementService.getDeviceOption();
        final String DEVICE_ARGUMENT = tunerManagementService.getDeviceArgument(tunerStatus);
        try{
            final File doRecordFile = new File(systemConfiguration.getFilePath() + FILE_SEPARATOR + doRecordFileName);
            log.info("doRecordFile: {}", doRecordFileName);
            if (!doRecordFile.exists()) {
                doRecordFile.createNewFile();
                final BufferedWriter bw = new BufferedWriter(new FileWriter(doRecordFile));
                bw.write("#!/bin/bash");
                bw.newLine();
                bw.write(systemConfiguration.getRecxxxPath() + " " + DEVICE_OPTION + " " + DEVICE_ARGUMENT + " " + physicalLogicalChannel + " " + duration + " \"" + systemConfiguration.getFilePath() + FILE_SEPARATOR + fileName + "\"" + " >/dev/null");
                bw.newLine();
                // TODO: separate sh into recoding and transcoding
//                bw.write(systemConfiguration.getFfmpegPath() +  " -i " + "\"" + systemConfiguration.getFilePath() + FILE_SEPARATOR + fileName + "\"" + " -ss " + thumbnailPoint + " -vframes 1 -f image2 " + "\"" + systemConfiguration.getFilePath() + FILE_SEPARATOR + fileName + ".jpg\"" + " >/dev/null");
//                bw.newLine();
                final HardwareAccelerationType hardwareAccelerationType = systemService.getHardwareAccelerationType();
                final String SPECIFIC_OPTIONS;
                if(hardwareAccelerationType == HardwareAccelerationType.H264_QSV){
                    SPECIFIC_OPTIONS = "h264_qsv";
                }else if(hardwareAccelerationType == HardwareAccelerationType.H264_NVENC){
                    SPECIFIC_OPTIONS = "h264_nvenc";
                }else if(hardwareAccelerationType == HardwareAccelerationType.H264_OMX){
                    SPECIFIC_OPTIONS = "h264_omx";
                }else if(hardwareAccelerationType == HardwareAccelerationType.H264_X264){
                    SPECIFIC_OPTIONS = "libx264";
                }else{
                    SPECIFIC_OPTIONS = "";
                }
                // TODO: separate sh into recoding and transcoding
//                bw.write(systemConfiguration.getFfmpegPath() + " -i " + "\"" + systemConfiguration.getFilePath() + FILE_SEPARATOR + fileName + "\"" + " -acodec aac -ab 160k -ar 44100 -ac 2 -s 1280x720 -vcodec " + SPECIFIC_OPTIONS + " -profile:v high -level 4.2 -b:v 2400k -threads 1 -y " + "\"" + systemConfiguration.getFilePath() + FILE_SEPARATOR + fileName + ".m4v\"" + " >/dev/null");
//                bw.newLine();
//                bw.write(systemConfiguration.getFfmpegPath() + " -i " + "\"" + systemConfiguration.getFilePath() + FILE_SEPARATOR + fileName + "\"" + " -acodec aac -ab 32k -ar 44100 -ac 2 -s 320x180 -vcodec " + SPECIFIC_OPTIONS + " -profile:v high -level 4.1 -b:v 160k -threads 1 -y " + "\"" + systemConfiguration.getFilePath() + FILE_SEPARATOR + fileName + ".watch.m4v\"" + " >/dev/null");
                bw.close();
            }

            final String[] chmod = {"chmod", "755", systemConfiguration.getFilePath() + FILE_SEPARATOR + doRecordFileName};
            final ProcessBuilder chmodProcessBuilder = new ProcessBuilder(chmod);
            final Process chmodProcess = chmodProcessBuilder.start();
            final InputStream chmodInputStream = chmodProcess.getErrorStream();
            final InputStreamReader chmodInputStreamReader = new InputStreamReader(chmodInputStream);
            final BufferedReader chmodBufferedReader = new BufferedReader(chmodInputStreamReader);
            String chmodString = "";
            while ((chmodString = chmodBufferedReader.readLine()) != null){
                log.info(chmodString);
            }
            chmodBufferedReader.close();
            chmodInputStreamReader.close();
            chmodInputStream.close();
            chmodProcess.destroy();

            final String[] run = {systemConfiguration.getFilePath() + FILE_SEPARATOR + doRecordFileName};
            final ProcessBuilder runProcessBuilder = new ProcessBuilder(run);
            final Process runProcess = runProcessBuilder.start();
            final InputStream runInputStream = runProcess.getErrorStream();
            final InputStreamReader runInputStreamReader = new InputStreamReader(runInputStream);
            final BufferedReader runBufferedReader = new BufferedReader(runInputStreamReader);
            String runString = "";
            while ((runString = runBufferedReader.readLine()) != null){
                log.info(runString);
            }
            runBufferedReader.close();
            runInputStreamReader.close();
            runInputStream.close();
            runProcess.destroy();
            log.info("recording is done.");

            doRecordFile.delete();

        }catch(IOException e){
            log.error("cannot run do-record.sh: {} {}", e.getMessage(), e);
            tunerManagementService.update(tunerStatus, true);
            return new AsyncResult<>(-1);
        }

        tunerManagementService.update(tunerStatus, true);
        return new AsyncResult<>(0);
    }

    @Override
    public void cancel() {

    }
}
