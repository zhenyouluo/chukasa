package pro.hirooka.chukasa.domain.service.epgdump.runner;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import pro.hirooka.chukasa.domain.configuration.EpgdumpConfiguration;
import pro.hirooka.chukasa.domain.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.model.chukasa.constants.ChukasaConstant;
import pro.hirooka.chukasa.domain.model.common.TunerStatus;
import pro.hirooka.chukasa.domain.model.epgdump.LastEpgdumpExecuted;
import pro.hirooka.chukasa.domain.model.recorder.ChannelConfiguration;
import pro.hirooka.chukasa.domain.model.recorder.enums.ChannelType;
import pro.hirooka.chukasa.domain.service.common.ITunerManagementService;
import pro.hirooka.chukasa.domain.service.epgdump.ILastEpgdumpExecutedService;
import pro.hirooka.chukasa.domain.service.epgdump.parser.IEpgdumpParser;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

@EnableAsync
@Slf4j
@Service
public class EpgdumpRunnerService implements IEpgdumpRunnerService {

    private final String FILE_SEPARATOR = ChukasaConstant.FILE_SEPARATOR;

    @Autowired
    private SystemConfiguration systemConfiguration;
    @Autowired
    private EpgdumpConfiguration epgdumpConfiguration;
    @Autowired
    private IEpgdumpParser epgdumpParser;
    @Autowired
    private ILastEpgdumpExecutedService lastEpgdumpExecutedService;
    @Autowired
    private ITunerManagementService tunerManagementService;

    @Async
    @Override
    public Future<Integer> submit(List<ChannelConfiguration> channelConfigurationList) {

        // TODO: null
        final TunerStatus tunerStatusGR = tunerManagementService.findOne(ChannelType.GR);
        final TunerStatus usingTunerStatusGR = tunerManagementService.update(tunerStatusGR, false);
        final TunerStatus tunerStatusBS = tunerManagementService.findOne(ChannelType.BS);
        final TunerStatus usingTunerStatusBS = tunerManagementService.update(tunerStatusBS, false);

        final File temporaryEpgdumpPathFile = new File(epgdumpConfiguration.getTemporaryPath());

        cleanupTemporaryEpgdumpPath(temporaryEpgdumpPathFile);
        if(temporaryEpgdumpPathFile.mkdirs()){
            log.info("epgdump temporary path: {}", temporaryEpgdumpPathFile);
        }else{
            log.error("cannot create epgdump temporary path: {}", temporaryEpgdumpPathFile);
            releaseTuner(tunerStatusGR);
            releaseTuner(tunerStatusBS);
            return new AsyncResult<>(-1);
        }
        final String epgdumpShellPath = epgdumpConfiguration.getTemporaryPath() + FILE_SEPARATOR + "epgdump.sh";

        final File epgdumpShellFile = new File(epgdumpShellPath);
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(epgdumpShellFile));
            bufferedWriter.write("#!/bin/bash");
            bufferedWriter.newLine();
            final String DEVICE_OPTION = tunerManagementService.getDeviceOption();
            boolean isBS = false;
            for(ChannelConfiguration channelConfiguration : channelConfigurationList){
                if(channelConfiguration.getChannelType() == ChannelType.GR || !isBS) {
                    try {
                        final int physicalLogicalChannel = channelConfiguration.getPhysicalLogicalChannel();
                        final String recxxxCommand;
                        if(channelConfiguration.getChannelType() == ChannelType.GR) {
                            final String DEVICE_ARGUMENT = tunerManagementService.getDeviceArgument(usingTunerStatusGR);
                            recxxxCommand = systemConfiguration.getRecxxxPath() + " " + DEVICE_OPTION + " " + DEVICE_ARGUMENT + " " + physicalLogicalChannel + " " + epgdumpConfiguration.getRecordingDuration() + " " + epgdumpConfiguration.getTemporaryPath() + FILE_SEPARATOR + "epgdump" + physicalLogicalChannel + ".ts";
                        }else if(channelConfiguration.getChannelType() == ChannelType.BS){
                            final String DEVICE_ARGUMENT = tunerManagementService.getDeviceArgument(usingTunerStatusBS);
                            recxxxCommand = systemConfiguration.getRecxxxPath() + " " + DEVICE_OPTION + " " + DEVICE_ARGUMENT + " " + physicalLogicalChannel + " " + epgdumpConfiguration.getRecordingDuration() + " " + epgdumpConfiguration.getTemporaryPath() + FILE_SEPARATOR + "epgdump" + physicalLogicalChannel + ".ts";
                        }else{
                            log.error("unknown ChannelType");
                            releaseTuner(tunerStatusGR);
                            releaseTuner(tunerStatusBS);
                            return new AsyncResult<>(-1);
                        }
                        final String epgdumpCommand = epgdumpConfiguration.getPath() + " json " + epgdumpConfiguration.getTemporaryPath()+ FILE_SEPARATOR + "epgdump" + physicalLogicalChannel + ".ts " + epgdumpConfiguration.getTemporaryPath() + FILE_SEPARATOR + "epgdump" + physicalLogicalChannel + ".json";
                        bufferedWriter.write(recxxxCommand);
                        bufferedWriter.newLine();
                        bufferedWriter.write(epgdumpCommand);
                        bufferedWriter.newLine();
                        if(channelConfiguration.getChannelType() == ChannelType.BS){
                            isBS = true;
                        }
                    } catch (NumberFormatException e) {
                        log.error("invalid value", e.getMessage(), e);
                    }
                }
            }
            bufferedWriter.close();

            final String[] chmodCommandArray = {"chmod", "755", epgdumpShellPath};
            executeCommand(chmodCommandArray);

            final long begin = System.currentTimeMillis();

            final String[] epgdumpCommandArray = {epgdumpShellPath};
            executeCommand(epgdumpCommandArray);

            for(ChannelConfiguration channelConfiguration : channelConfigurationList) {
                final String jsonStringPath = epgdumpConfiguration.getTemporaryPath() + FILE_SEPARATOR + "epgdump" + channelConfiguration.getPhysicalLogicalChannel() + ".json";
                if(new File(jsonStringPath).exists() && new File(jsonStringPath).length() > 0) {
                    try {
                        epgdumpParser.parse(jsonStringPath, channelConfiguration.getPhysicalLogicalChannel(), channelConfiguration.getRemoteControllerChannel());
                    } catch (IOException e) {
                        log.error("cannot parse epgdump output: {} {}", e.getMessage(), e);
                        releaseTuner(tunerStatusGR);
                        releaseTuner(tunerStatusBS);
                        return new AsyncResult<>(-1);
                    }
                }else{
                    log.error("no epgdump output JSON file: {}", jsonStringPath);
                    releaseTuner(tunerStatusGR);
                    releaseTuner(tunerStatusBS);
                    return new AsyncResult<>(-1);
                }
            }

            final long end = System.currentTimeMillis();
            log.info((end - begin) / 1000 + "s");

            final LastEpgdumpExecuted previousLastEpgdumpExecuted = lastEpgdumpExecutedService.read(1);
            final LastEpgdumpExecuted newLastEpgdumpExecuted;
            final Date date = new Date();
            if (previousLastEpgdumpExecuted == null) {
                newLastEpgdumpExecuted = new LastEpgdumpExecuted();
                newLastEpgdumpExecuted.setDate(date.getTime());
                newLastEpgdumpExecuted.setUnique(1);
            }else{
                previousLastEpgdumpExecuted.setDate(date.getTime());
                newLastEpgdumpExecuted = previousLastEpgdumpExecuted;
            }
            lastEpgdumpExecutedService.update(newLastEpgdumpExecuted);
            log.info("lastEpgdumpExecuted = {}", newLastEpgdumpExecuted.getDate());

            cleanupTemporaryEpgdumpPath(temporaryEpgdumpPathFile);

            releaseTuner(tunerStatusGR);
            releaseTuner(tunerStatusBS);
            return new AsyncResult<>(0);

        } catch (IOException e) {
            log.error("{} {}", e.getMessage(), e);
            releaseTuner(tunerStatusGR);
            releaseTuner(tunerStatusBS);
            return new AsyncResult<>(-1);
        }
    }

    @Override
    public void cancel() {

    }

    private void executeCommand(String[] commandArray){
        final ProcessBuilder processBuilder = new ProcessBuilder(commandArray);
        try {
            final Process process = processBuilder.start();
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s = "";
            while((s = bufferedReader.readLine()) != null){
                log.info("{}", s);
            }
            bufferedReader.close();
            process.destroy();
        } catch (IOException e) {
            log.error("{} {}", e.getMessage(), e);
        }
    }

    private void releaseTuner(TunerStatus tunerStatus){
        tunerManagementService.update(tunerStatus, true);
    }

    private boolean cleanupTemporaryEpgdumpPath(File file){
        try {
            if(file.exists()) {
                FileUtils.cleanDirectory(file);
                if (file.delete()) {
                    return true;
                } else {
                    log.info("cannot delete temporary epgdump path: {}", file.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            log.info("cannot clean temporary epgdump path:{} {}", e.getMessage(), e);
        }
        return false;
    }
}
