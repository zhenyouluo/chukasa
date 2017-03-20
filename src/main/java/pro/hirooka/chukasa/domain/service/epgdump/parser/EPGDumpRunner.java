package pro.hirooka.chukasa.domain.service.epgdump.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import pro.hirooka.chukasa.domain.configuration.EpgdumpConfiguration;
import pro.hirooka.chukasa.domain.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.model.epgdump.LastEpgdumpExecuted;
import pro.hirooka.chukasa.domain.model.recorder.ChannelConfiguration;
import pro.hirooka.chukasa.domain.model.recorder.enums.ChannelType;
import pro.hirooka.chukasa.domain.service.epgdump.ILastEpgdumpExecutedService;

import java.io.*;
import java.util.Date;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Slf4j
public class EPGDumpRunner implements Runnable {

    static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private final SystemConfiguration systemConfiguration;
    private final EpgdumpConfiguration epgdumpConfiguration;
    private final IEpgdumpParser epgdumpParser;
    private final ILastEpgdumpExecutedService lastEpgdumpExecutedService;
  //  private final Map<String, String> epgDumpChannelMap;
    private final List<ChannelConfiguration> channelConfigurationList;

    public EPGDumpRunner(
            SystemConfiguration systemConfiguration,
            EpgdumpConfiguration epgdumpConfiguration,
            IEpgdumpParser epgdumpParser,
            ILastEpgdumpExecutedService lastEpgdumpExecutedService,
            List<ChannelConfiguration> channelConfigurationList
//            Map<String, String> epgDumpChannelMap
    ){
        this.systemConfiguration = requireNonNull(systemConfiguration, "systemConfiguration");
        this.epgdumpConfiguration = requireNonNull(epgdumpConfiguration, "epgdumpConfiguration");
        this.epgdumpParser = requireNonNull(epgdumpParser, "epgdumpParser");
        this.lastEpgdumpExecutedService = requireNonNull(lastEpgdumpExecutedService, "lastEpgdumpExecutedService");
        this.channelConfigurationList = requireNonNull(channelConfigurationList, "channelConfigurationList");
//        this.epgDumpChannelMap = requireNonNull(epgDumpChannelMap, "epgDumpChannelMap");
    }

    @Override
    public void run() {
        execute();
    }

    void execute(){

//        Integer[] physicalChannelArray = chukasaConfiguration.getPhysicalChannel();
//        List<Integer> physicalChannelList = Arrays.asList(physicalChannelArray);

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
                        String recpt1Command = systemConfiguration.getRecxxxPath() + " --b25 --strip " + physicalChannel + " " + epgdumpConfiguration.getRecordingDuration() + " " + epgdumpConfiguration.getTemporaryPath() + FILE_SEPARATOR + "epgdump" + physicalChannel + ".ts";
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
//            for(Map.Entry<String, String> entry : epgDumpChannelMap.entrySet()) {
//                if(entry.getValue().equals("GR") || !isBS) {
//                    try {
//                        int physicalChannel = Integer.parseInt(entry.getKey());
//                        String recpt1Command = systemConfiguration.getRecpt1Path() + " --b25 --strip " + physicalChannel + " " + epgdumpConfiguration.getRecordingDuration() + " " + epgdumpConfiguration.getTemporaryPath() + FILE_SEPARATOR + "epgdump" + physicalChannel + ".ts";
//                        String epgdumpCommand = epgdumpConfiguration.getPath() + " json " + epgdumpConfiguration.getTemporaryPath()+ FILE_SEPARATOR + "epgdump" + physicalChannel + ".ts " + epgdumpConfiguration.getTemporaryPath() + FILE_SEPARATOR + "epgdump" + physicalChannel + ".json";
//                        bufferedWriter.write(recpt1Command);
//                        bufferedWriter.newLine();
//                        bufferedWriter.write(epgdumpCommand);
//                        bufferedWriter.newLine();
//                        if(entry.getValue().equals("BS")){
//                            isBS = true;
//                        }
//                    } catch (NumberFormatException e) {
//                        log.error("invalid value", e.getMessage(), e);
//                    }
//                }
//            }
//            for(int physicalChannel : physicalChannelList){
//                String recpt1Command = systemConfiguration.getRecpt1Path() + " --b25 --strip " + physicalChannel + " 128 " + systemConfiguration.getTempPath() + FILE_SEPARATOR + "epgdump" + physicalChannel + ".ts";
//                String epgdumpCommand = systemConfiguration.getEpgdumpPath() + " json " + systemConfiguration.getTempPath() + FILE_SEPARATOR + "epgdump" + physicalChannel + ".ts " + systemConfiguration.getTempPath() + FILE_SEPARATOR + "epgdump" + physicalChannel + ".json";
//                bufferedWriter.write(recpt1Command);
//                bufferedWriter.newLine();
//                bufferedWriter.write(epgdumpCommand);
//                bufferedWriter.newLine();
//            }
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
                    return;
                }
            }
        }
//        for(Map.Entry<String, String> entry : epgDumpChannelMap.entrySet()) {
//            String jsonStringPath = epgdumpConfiguration.getTemporaryPath() + FILE_SEPARATOR + "epgdump" + entry.getKey() + ".json";
//            if(new File(jsonStringPath).exists()) {
//                try {
//                    epgdumpParser.parse(jsonStringPath, Integer.parseInt(entry.getKey()), epgDumpChannelMap);
//                } catch (IOException e) {
//                    log.error("{} {}", e.getMessage(), e);
//                    return;
//                }
//            }
//        }
//        for(int physicalChannel : physicalChannelList){
//            String jsonStringPath = systemConfiguration.getTempPath() + FILE_SEPARATOR + "epgdump" + physicalChannel + ".json";
//            epgDumpParser.parse(jsonStringPath, epgDumpChannelMap);
//        }

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
    }
}