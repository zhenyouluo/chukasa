package pro.hirooka.chukasa.recorder;

import lombok.extern.slf4j.Slf4j;
import pro.hirooka.chukasa.domain.ReservedProgram;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RecorderChecker {

    static public boolean isAlreadyRun(ReservedProgram reservedProgram){

        List<String> command = new ArrayList<>();
        command.add("/bin/sh");
        command.add("-c");
        command.add("ps aux | grep recpt1");

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            Process process = processBuilder.start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String str = "";
            while((str = bufferedReader.readLine()) != null){
                if(str.contains(reservedProgram.getBeginDate()) && str.contains(reservedProgram.getEndDate()) && str.contains(reservedProgram.getCh() + "ch")){
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
