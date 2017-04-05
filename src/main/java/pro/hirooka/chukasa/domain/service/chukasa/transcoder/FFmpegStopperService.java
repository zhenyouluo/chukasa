package pro.hirooka.chukasa.domain.service.chukasa.transcoder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import pro.hirooka.chukasa.domain.service.chukasa.IChukasaModelManagementComponent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

@Slf4j
@Service
public class FFmpegStopperService implements IFFmpegStopperService {

    @Autowired
    IChukasaModelManagementComponent chukasaModelManagementComponent;

    @Override
    public Future<Integer> stop() {

        final String[] commandArray = {"ps", "aux"};
        final ProcessBuilder processBuilder = new ProcessBuilder(commandArray);
        try {
            final Process process = processBuilder.start();
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String str;
            while((str = bufferedReader.readLine()) != null){
                log.debug("{}", str);
                final String trimmedString = str.trim();
                // TODO: マズー
                if ((trimmedString.matches(".*libx264.*") && trimmedString.matches(".*mpegts.*")) || (trimmedString.matches(".*h264_qsv.*") && trimmedString.matches(".*mpegts.*")) || (trimmedString.matches(".*h264_omx.*") && trimmedString.matches(".*mpegts.*")) || (trimmedString.matches(".*h264_nvenc.*") && trimmedString.matches(".*hls.*"))) {
                    final String[] trimmedStringArray = trimmedString.split(" ");
                    final List<String> pidList = new ArrayList<>();
                    for(int i = 0; i < trimmedStringArray.length; i++) {
                        if (!(trimmedStringArray[i].equals(""))) {
                            pidList.add(trimmedStringArray[i]);
                        }
                    }
                    String pid = pidList.get(1);
                    log.debug("{}", pid);
                    stopPID(pid);
                }
            }
            bufferedReader.close();
            process.getInputStream().close();
            process.getErrorStream().close();
            process.getOutputStream().close();
            process.destroy();
        } catch (IOException e) {
            // TODO:
        }
        chukasaModelManagementComponent.deleteAll();
        log.info("all ChukasaModels have been deleted.");

        return new AsyncResult<>(0);
    }

    private void stopPID(String pid){

        final String[] commandArray = {"kill", "-KILL", pid };
        final ProcessBuilder processBuilder = new ProcessBuilder(commandArray);
        try {
            final Process process = processBuilder.start();
            log.info("{} stopped ffmpeg (PID: {}).", this.getClass().getName(), pid);
//            process.getInputStream().close();
//            process.getErrorStream().close();
//            process.getOutputStream().close();
//            process.destroy();
        } catch (IOException e) {
            log.error("{} {}", e.getMessage(), e);
        }
    }

}
