package pro.hirooka.chukasa.epgdump;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import pro.hirooka.chukasa.configuration.ChukasaConfiguration;
import pro.hirooka.chukasa.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.LastEPGDumpExecuted;
import pro.hirooka.chukasa.service.ILastEPGDumpExecutedService;

import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Slf4j
public class EPGDumpRunner implements Runnable {

    static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private final SystemConfiguration systemConfiguration;
    private final ChukasaConfiguration chukasaConfiguration;
    private final IEPGDumpParser epgDumpParser;
    private final ILastEPGDumpExecutedService lastEPGDumpExecutedService;

    private Map<String, Integer> epgDumpChannelMap;

    @Autowired
    public EPGDumpRunner(
            SystemConfiguration systemConfiguration,
            ChukasaConfiguration chukasaConfiguration,
            IEPGDumpParser epgDumpParser,
            ILastEPGDumpExecutedService lastEPGDumpExecutedService,
            Map<String, Integer> epgDumpChannelMap){
        this.systemConfiguration = requireNonNull(systemConfiguration, "systemConfiguration");
        this.chukasaConfiguration = requireNonNull(chukasaConfiguration, "chukasaConfiguration");
        this.epgDumpParser = requireNonNull(epgDumpParser, "epgDumpParser");
        this.lastEPGDumpExecutedService = requireNonNull(lastEPGDumpExecutedService, "lastEPGDumpExecutedService");
        this.epgDumpChannelMap = requireNonNull(epgDumpChannelMap, "epgDumpChannelMap");
    }

    @Override
    public void run() {
        execute();
    }

    void execute(){

//        Integer[] physicalChannelArray = chukasaConfiguration.getPhysicalChannel();
//        List<Integer> physicalChannelList = Arrays.asList(physicalChannelArray);

        String epgdumpShell = systemConfiguration.getTempPath() + FILE_SEPARATOR + "epgdump.sh";

        File file = new File(epgdumpShell);
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("#!/bin/bash");
            bufferedWriter.newLine();
            for(Map.Entry<String, Integer> entry : epgDumpChannelMap.entrySet()) {
                int physicalChannel = entry.getValue();
                String recpt1Command = systemConfiguration.getRecpt1Path() + " --b25 --strip " + physicalChannel + " 128 " + systemConfiguration.getTempPath() + FILE_SEPARATOR + "epgdump" + physicalChannel + ".ts";
                String epgdumpCommand = systemConfiguration.getEpgdumpPath() + " json " + systemConfiguration.getTempPath() + FILE_SEPARATOR + "epgdump" + physicalChannel + ".ts " + systemConfiguration.getTempPath() + FILE_SEPARATOR + "epgdump" + physicalChannel + ".json";
                bufferedWriter.write(recpt1Command);
                bufferedWriter.newLine();
                bufferedWriter.write(epgdumpCommand);
                bufferedWriter.newLine();
            }
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
                    log.debug("{}", s);
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
                    log.debug("{}", s);
                }
                bufferedReader.close();
                process.destroy();
            } catch (IOException e) {
                log.error("{} {}", e.getMessage(), e);
            }
        }

        for(Map.Entry<String, Integer> entry : epgDumpChannelMap.entrySet()) {
            String jsonStringPath = systemConfiguration.getTempPath() + FILE_SEPARATOR + "epgdump" + entry.getValue() + ".json";
            epgDumpParser.parse(jsonStringPath, epgDumpChannelMap);
        }
//        for(int physicalChannel : physicalChannelList){
//            String jsonStringPath = systemConfiguration.getTempPath() + FILE_SEPARATOR + "epgdump" + physicalChannel + ".json";
//            epgDumpParser.parse(jsonStringPath, epgDumpChannelMap);
//        }

        long end = System.currentTimeMillis();
        log.info((end - begin) / 1000 + "s");


        LastEPGDumpExecuted lastEPGDumpExecuted = lastEPGDumpExecutedService.read(1);
        if (lastEPGDumpExecuted == null) {
            lastEPGDumpExecuted = new LastEPGDumpExecuted();
            lastEPGDumpExecuted.setUnique(1);
        }
        Date date = new Date();
        lastEPGDumpExecuted.setDate(date.getTime());
        lastEPGDumpExecuted = lastEPGDumpExecutedService.update(lastEPGDumpExecuted);
        log.info("lastEPGDumpExecuted = {}", lastEPGDumpExecuted.getDate());
    }
}