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
import pro.hirooka.chukasa.domain.model.common.enums.RecxxxDriverType;
import pro.hirooka.chukasa.domain.model.epgdump.LastEpgdumpExecuted;
import pro.hirooka.chukasa.domain.model.epgdump.RecdvbBSModel;
import pro.hirooka.chukasa.domain.model.recorder.ChannelConfiguration;
import pro.hirooka.chukasa.domain.model.recorder.enums.ChannelType;
import pro.hirooka.chukasa.domain.service.common.ITunerManagementService;
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
    private ITunerManagementService tunerManagementService;

    @Async
    @Override
    public Future<Integer> submit(List<ChannelConfiguration> channelConfigurationList) {

        final RecxxxDriverType recxxxDriverType = tunerManagementService.getRecxxxDriverType();
        TunerStatus tunerStatusGR = tunerManagementService.findOne(ChannelType.GR);
        tunerStatusGR = tunerManagementService.update(tunerStatusGR, false);
        TunerStatus tunerStatusBS = tunerManagementService.findOne(ChannelType.BS);
        tunerStatusBS = tunerManagementService.update(tunerStatusBS, false);

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
                        final int physicalLogicalChannel = channelConfiguration.getPhysicalLogicalChannel();
                        final String recxxxCommand;
                        if(recxxxDriverType == RecxxxDriverType.DVB){
                            if(channelConfiguration.getChannelType() == ChannelType.GR) {
                                recxxxCommand = systemConfiguration.getRecxxxPath() + " --dev " + tunerStatusGR.getIndex() + " " + physicalLogicalChannel + " " + epgdumpConfiguration.getRecordingDuration() + " " + epgdumpConfiguration.getTemporaryPath() + FILE_SEPARATOR + "epgdump" + physicalLogicalChannel + ".ts";
                            }else if(channelConfiguration.getChannelType() == ChannelType.BS){
                                RecdvbBSModel recdvbBSModel = epgdumpRecdvbHelper.resovle(physicalLogicalChannel);
                                recxxxCommand = systemConfiguration.getRecxxxPath() + " --dev " + tunerStatusBS.getIndex() + " --tsid " + recdvbBSModel.getTsid() + " " + recdvbBSModel.getName() + " " + epgdumpConfiguration.getRecordingDuration() + " " + epgdumpConfiguration.getTemporaryPath() + FILE_SEPARATOR + "epgdump" + physicalLogicalChannel + ".ts";
                            }else{
                                recxxxCommand = "";
                            }
                        }else{
                            recxxxCommand = systemConfiguration.getRecxxxPath() + " " + physicalLogicalChannel + " " + epgdumpConfiguration.getRecordingDuration() + " " + epgdumpConfiguration.getTemporaryPath() + FILE_SEPARATOR + "epgdump" + physicalLogicalChannel + ".ts";

                        }
                        String epgdumpCommand = epgdumpConfiguration.getPath() + " json " + epgdumpConfiguration.getTemporaryPath()+ FILE_SEPARATOR + "epgdump" + physicalLogicalChannel + ".ts " + epgdumpConfiguration.getTemporaryPath() + FILE_SEPARATOR + "epgdump" + physicalLogicalChannel + ".json";
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

        tunerManagementService.update(tunerStatusGR, true);
        tunerManagementService.update(tunerStatusBS, true);

        return new AsyncResult<>(0);
    }

    @Override
    public void cancel() {

    }
}
