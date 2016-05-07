package pro.hirooka.chukasa.epgdump;

import lombok.extern.slf4j.Slf4j;
import pro.hirooka.chukasa.parser.IEPGDumpParser;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class EPGDumpRunner implements Runnable {

    static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private final IEPGDumpParser epgDumpParser;

    private Map<String, Integer> epgDumpChannelMap;

    public EPGDumpRunner(IEPGDumpParser epgDumpParser, Map<String, Integer> epgDumpChannelMap){
        this.epgDumpParser = epgDumpParser;
        this.epgDumpChannelMap = epgDumpChannelMap;
    }

    @Override
    public void run() {

        Integer[] physicalChannelArray = {27};
        List<Integer> physicalChannelList = Arrays.asList(physicalChannelArray);

//        String epgdumpShell = chukasaModel.getSystemConfiguration().getTempPath() + FILE_SEPARATOR + "epgdump.sh";
        String epgdumpShell = "/tmp/chukasa" + FILE_SEPARATOR + "epgdump.sh";

        File file = new File(epgdumpShell);
        BufferedWriter bufferedWriter;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("#!/bin/bash");
            bufferedWriter.newLine();
            for(int physicalChannel : physicalChannelList){
                String recpt1Command = "recpt1 --b25 --strip " + physicalChannel + " 128 /tmp/chukasa/epgdump" + physicalChannel + ".ts";
                String epgdumpCommand = "epgdump json /tmp/chukasa/epgdump" + physicalChannel + ".ts /tmp/chukasa/epgdump" + physicalChannel + ".json";
                bufferedWriter.write(recpt1Command);
                bufferedWriter.newLine();
                bufferedWriter.write(epgdumpCommand);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        if(true){
            String[] chmodCommandArray = {"chmod", "755", epgdumpShell};
            ProcessBuilder processBuilder = new ProcessBuilder(chmodCommandArray);
            Process process;
            try {
                process = processBuilder.start();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String s = "";
                while((s = bufferedReader.readLine()) != null){
                    log.debug("{}", s);
                }
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(true){
            String[] epgdumpCommandArray = {epgdumpShell};
            ProcessBuilder processBuilder = new ProcessBuilder(epgdumpCommandArray);
            Process process;
            try {
                process = processBuilder.start();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String s = "";
                while((s = bufferedReader.readLine()) != null){
                    log.debug("{}", s);
                }
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for(int physicalChannel : physicalChannelList){
            String jsonStringPath = "/tmp/chukasa/epgdump" + physicalChannel + ".json";
            epgDumpParser.parse(jsonStringPath);
        }
    }
}