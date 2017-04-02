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
import pro.hirooka.chukasa.domain.model.common.Tuner;
import pro.hirooka.chukasa.domain.model.epgdump.LastEpgdumpExecuted;
import pro.hirooka.chukasa.domain.model.epgdump.RecdvbBSModel;
import pro.hirooka.chukasa.domain.model.recorder.ChannelConfiguration;
import pro.hirooka.chukasa.domain.model.recorder.enums.ChannelType;
import pro.hirooka.chukasa.domain.service.common.ulitities.CommonUtilityService;
import pro.hirooka.chukasa.domain.service.epgdump.ILastEpgdumpExecutedService;
import pro.hirooka.chukasa.domain.service.epgdump.parser.IEpgdumpParser;
import pro.hirooka.chukasa.domain.service.epgdump.runner.helper.IEpgdumpRecdvbHelper;

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
    private IEpgdumpRecdvbHelper epgdumpRecdvbHelper;
    @Autowired
    private CommonUtilityService commonUtilityService;

    @Async
    @Override
    public Future<Integer> submit(List<ChannelConfiguration> channelConfigurationList) {

        final List<Tuner> tunerList = commonUtilityService.getTunerList();
        boolean isDVB = false;
        for(Tuner tuner : tunerList){
            if(tuner.getDeviceName().startsWith("/dev/dvb/adapter")){
                isDVB = true;
                break;
            }
        }

        File temporaryEpgdumpPathFile = new File(epgdumpConfiguration.getTemporaryPath());
        if(temporaryEpgdumpPathFile.mkdirs()){
            //
        }else{
            //
        }
        String epgdumpShell = epgdumpConfiguration.getTemporaryPath() + FILE_SEPARATOR + "epgdump.sh";

        File file = new File(epgdumpShell);
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("#!/bin/bash");
            bufferedWriter.newLine();
            boolean isBS = false;
            for(ChannelConfiguration channelConfiguration : channelConfigurationList){
                if(channelConfiguration.getChannelType() == ChannelType.GR || !isBS) {
                    try {
                        int physicalChannel = channelConfiguration.getPhysicalLogicalChannel();
                        final String recpt1Command;
                        if(!isDVB) {
                            recpt1Command = systemConfiguration.getRecxxxPath() + " " + physicalChannel + " " + epgdumpConfiguration.getRecordingDuration() + " " + epgdumpConfiguration.getTemporaryPath() + FILE_SEPARATOR + "epgdump" + physicalChannel + ".ts";
                        }else{
                            if(channelConfiguration.getChannelType() == ChannelType.GR) {
                                int deviceIndex = 0;
                                for(Tuner tuner : tunerList){
                                    if(tuner.getChannelType() == ChannelType.GR){
                                        deviceIndex = Integer.parseInt(tuner.getDeviceName().split("adapter")[1]); // TODO: exception
                                        break;
                                    }
                                }
                                recpt1Command = systemConfiguration.getRecxxxPath() + " --dev " + Integer.toString(deviceIndex) + " " + physicalChannel + " " + epgdumpConfiguration.getRecordingDuration() + " " + epgdumpConfiguration.getTemporaryPath() + FILE_SEPARATOR + "epgdump" + physicalChannel + ".ts";
                            }else if(channelConfiguration.getChannelType() == ChannelType.BS){
                                int deviceIndex = 0;
                                for(Tuner tuner : tunerList){
                                    if(tuner.getChannelType() == ChannelType.BS){
                                        deviceIndex = Integer.parseInt(tuner.getDeviceName().split("adapter")[1]); // TODO: exception
                                        break;
                                    }
                                }
                                RecdvbBSModel recdvbBSModel = epgdumpRecdvbHelper.resovle(physicalChannel);
                                recpt1Command = systemConfiguration.getRecxxxPath() + " --dev " + Integer.toString(deviceIndex) + " --tsid " + recdvbBSModel.getTsid() + " " + recdvbBSModel.getName() + " " + epgdumpConfiguration.getRecordingDuration() + " " + epgdumpConfiguration.getTemporaryPath() + FILE_SEPARATOR + "epgdump" + physicalChannel + ".ts";
                            }else{
                                recpt1Command = "";
                            }
                        }
                        String epgdumpCommand = epgdumpConfiguration.getPath() + " json " + epgdumpConfiguration.getTemporaryPath()+ FILE_SEPARATOR + "epgdump" + physicalChannel + ".ts " + epgdumpConfiguration.getTemporaryPath() + FILE_SEPARATOR + "epgdump" + physicalChannel + ".json";
                        bufferedWriter.write(recpt1Command);
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
        } catch (IOException e) {
            log.error("{} {}", e.getMessage(), e);
        }

        if(true){
            String[] chmodCommandArray = {"chmod", "755", epgdumpShell};
            ProcessBuilder processBuilder = new ProcessBuilder(chmodCommandArray);
            try {
                Process process = processBuilder.start();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
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

        long begin = System.currentTimeMillis();

        if(true){
            String[] epgdumpCommandArray = {epgdumpShell};
            ProcessBuilder processBuilder = new ProcessBuilder(epgdumpCommandArray);
            try {
                Process process = processBuilder.start();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
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

        for(ChannelConfiguration channelConfiguration : channelConfigurationList) {
            String jsonStringPath = epgdumpConfiguration.getTemporaryPath() + FILE_SEPARATOR + "epgdump" + channelConfiguration.getPhysicalLogicalChannel() + ".json";
            if(new File(jsonStringPath).exists()) {
                try {
                    epgdumpParser.parse(jsonStringPath, channelConfiguration.getPhysicalLogicalChannel(), channelConfiguration.getRemoteControllerChannel());
                } catch (IOException e) {
                    log.error("{} {}", e.getMessage(), e);
                    return new AsyncResult<>(-1);
                }
            }
        }

        long end = System.currentTimeMillis();
        log.info((end - begin) / 1000 + "s");


        LastEpgdumpExecuted lastEpgdumpExecuted = lastEpgdumpExecutedService.read(1);
        if (lastEpgdumpExecuted == null) {
            lastEpgdumpExecuted = new LastEpgdumpExecuted();
            lastEpgdumpExecuted.setUnique(1);
        }
        Date date = new Date();
        lastEpgdumpExecuted.setDate(date.getTime());
        lastEpgdumpExecuted = lastEpgdumpExecutedService.update(lastEpgdumpExecuted);
        log.info("lastEpgdumpExecuted = {}", lastEpgdumpExecuted.getDate());

        try {
            FileUtils.cleanDirectory(temporaryEpgdumpPathFile);
            temporaryEpgdumpPathFile.delete();
        } catch (IOException e) {
            log.error("{} {}", e.getMessage(), e);
        }
        return new AsyncResult<>(0);
    }

    @Override
    public void cancel() {

    }
}
