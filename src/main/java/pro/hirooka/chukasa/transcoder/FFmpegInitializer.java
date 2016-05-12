package pro.hirooka.chukasa.transcoder;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;

@Slf4j
public class FFmpegInitializer implements Runnable {

    private long pid;

    public FFmpegInitializer(long pid) {
        this.pid = pid;
    }

    @Override
    public void run() {

        String[] cmdArrayPID = {"kill", "-KILL", Long.toString(pid) };
        ProcessBuilder pbPID = new ProcessBuilder(cmdArrayPID);
        try {
            Process prPID = pbPID.start();
            log.info("{} stopped ffmpeg (PID: {}).", this.getClass().getName(), pid);
        } catch (IOException e) {
            log.error("{} {}", e.getMessage(), e);
        }
    }
}
