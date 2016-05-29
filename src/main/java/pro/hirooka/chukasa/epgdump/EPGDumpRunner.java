package pro.hirooka.chukasa.epgdump;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import pro.hirooka.chukasa.configuration.ChukasaConfiguration;
import pro.hirooka.chukasa.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.epgdump.LastEpgdumpExecuted;
import pro.hirooka.chukasa.service.epgdump.ILastEpgdumpExecutedService;

import java.io.*;
import java.util.Date;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Slf4j
public class EPGDumpRunner implements Runnable {

    static final String FILE_SEPARATOR = System.getProperty("file.separator");

    @Autowired
    SystemConfiguration systemConfiguration;
    @Autowired
    ChukasaConfiguration chukasaConfiguration;
    @Autowired
    IEpgdumpParser epgDumpParser;
    @Autowired
    ILastEpgdumpExecutedService lastEpgdumpExecutedService;

    private Map<String, Integer> epgDumpChannelMap;

    public EPGDumpRunner(Map<String, Integer> epgDumpChannelMap){
        this.epgDumpChannelMap = requireNonNull(epgDumpChannelMap, "epgDumpChannelMap");
    }

    @Override
    public void run() {
        execute();
    }

    void execute(){

//        Integer[] physicalChannelArray = chukasaConfiguration.getPhysicalChannel();
//        List<Integer> physicalChannelList = Arrays.asList(physicalChannelArray);

        File temporaryEpgdumpPathFile = new File(systemConfiguration.getTempEpgdumpPath());
        if(temporaryEpgdumpPathFile.mkdirs()){
            //
        }else{
            //
        }
        String epgdumpShell = systemConfiguration.getTempEpgdumpPath() + FILE_SEPARATOR + "epgdump.sh";

        File file = new File(epgdumpShell);
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("#!/bin/bash");
            bufferedWriter.newLine();
            for(Map.Entry<String, Integer> entry : epgDumpChannelMap.entrySet()) {
                int physicalChannel = entry.getValue();
                String recpt1Command = systemConfiguration.getRecpt1Path() + " --b25 --strip " + physicalChannel + " 128 " + systemConfiguration.getTempEpgdumpPath() + FILE_SEPARATOR + "epgdump" + physicalChannel + ".ts";
                String epgdumpCommand = systemConfiguration.getEpgdumpPath() + " json " + systemConfiguration.getTempEpgdumpPath() + FILE_SEPARATOR + "epgdump" + physicalChannel + ".ts " + systemConfiguration.getTempEpgdumpPath() + FILE_SEPARATOR + "epgdump" + physicalChannel + ".json";
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
            String jsonStringPath = systemConfiguration.getTempEpgdumpPath() + FILE_SEPARATOR + "epgdump" + entry.getValue() + ".json";
            epgDumpParser.parse(jsonStringPath, epgDumpChannelMap);
        }
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