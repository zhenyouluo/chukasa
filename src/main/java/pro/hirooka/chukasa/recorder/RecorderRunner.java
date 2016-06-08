package pro.hirooka.chukasa.recorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import pro.hirooka.chukasa.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.recorder.ReservedProgram;

import java.io.*;
import java.util.Date;

import static java.util.Objects.requireNonNull;

@Slf4j
public class RecorderRunner implements Runnable {

    static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private final SystemConfiguration systemConfiguration;

    private ReservedProgram reservedProgram;

    public RecorderRunner(SystemConfiguration systemConfiguration, ReservedProgram reservedProgram){
        this.systemConfiguration = requireNonNull(systemConfiguration, "systemConfiguration");
        this.reservedProgram = requireNonNull(reservedProgram, "reservedProgram");
    }

    @Override
    public void run() {

        log.info("start recording... ");

        int ch = reservedProgram.getCh();
        long startRecording = reservedProgram.getStartRecording();
        long stopRecording = reservedProgram.getStopRecording();
        long duration = reservedProgram.getDurationRecording();
        String title = reservedProgram.getTitle();

        long now = new Date().getTime();

        // start recording immediately
        // Create do-record.sh (do-record_ch_yyyyMMdd_yyyyMMdd.sh)
        String doRecordFileName = "do-record_" + ch + "_" + startRecording + "_" + stopRecording + ".sh";
        try{
            File doRecordFile = new File(systemConfiguration.getFilePath() + FILE_SEPARATOR + doRecordFileName);
            log.info("doRecordFile: {}", doRecordFileName);
            if (!doRecordFile.exists()) {
                doRecordFile.createNewFile();
                BufferedWriter bw = new BufferedWriter(new FileWriter(doRecordFile));
                bw.write("#!/bin/bash");
                bw.newLine();
                bw.write(systemConfiguration.getRecpt1Path() + " --b25 --strip " + ch + " " + duration + " \"" + systemConfiguration.getFilePath() + FILE_SEPARATOR + startRecording + "_" + stopRecording + "_" + duration  + "_" + title + "_"  + ch + "ch"  + ".ts\"" + " >/dev/null");
                bw.newLine();
                bw.write(systemConfiguration.getFfmpegPath() + " -ss 600 -vframes 1 -i " + "\"" + systemConfiguration.getFilePath() + FILE_SEPARATOR + startRecording + "_" + stopRecording + "_" + duration  + "_" + title + "_"  + ch + "ch"  + ".ts\"" + "-f image2 " + "\"" + systemConfiguration.getFilePath() + FILE_SEPARATOR + startRecording + "_" + stopRecording + "_" + duration  + "_" + title + "_"  + ch + "ch"  + ".ts.jpg\"" + " >/dev/null");
                bw.newLine();
                if(systemConfiguration.isQuickSyncVideoEnabled()) {
                    bw.write(systemConfiguration.getFfmpegPath() + " -i " + "\"" + systemConfiguration.getFilePath() + FILE_SEPARATOR + startRecording + "_" + stopRecording + "_" + duration + "_" + title + "_" + ch + "ch" + ".ts\"" + " -acodec aac -ab 160k -ar 44100 -ac 2 -s 1280x720 -vcodec h264_qsv -profile:v high -level 4.2 -b:v 24000k -threads 1 -y " + "\"" + systemConfiguration.getFilePath() + FILE_SEPARATOR + startRecording + "_" + stopRecording + "_" + duration + "_" + title + "_" + ch + "ch" + ".ts.mp4\"" + " >/dev/null");
                }
                bw.close();
            }

            String[] chmod = {"chmod", "755", systemConfiguration.getFilePath() + FILE_SEPARATOR + doRecordFileName};
            ProcessBuilder chmodProcessBuilder = new ProcessBuilder(chmod);
            Process chmodProcess = chmodProcessBuilder.start();
            InputStream chmodInputStream = chmodProcess.getErrorStream();
            InputStreamReader chmodInputStreamReader = new InputStreamReader(chmodInputStream);
            BufferedReader chmodBufferedReader = new BufferedReader(chmodInputStreamReader);
            String chmodString = "";
            while ((chmodString = chmodBufferedReader.readLine()) != null){
                log.info(chmodString);
            }
            chmodBufferedReader.close();
            chmodInputStreamReader.close();
            chmodInputStream.close();
            chmodProcess.destroy();

            String[] run = {systemConfiguration.getFilePath() + FILE_SEPARATOR + doRecordFileName};
            ProcessBuilder runProcessBuilder = new ProcessBuilder(run);
            Process runProcess = runProcessBuilder.start();
            InputStream runInputStream = runProcess.getErrorStream();
            InputStreamReader runInputStreamReader = new InputStreamReader(runInputStream);
            BufferedReader runBufferedReader = new BufferedReader(runInputStreamReader);
            String runString = "";
            while ((runString = runBufferedReader.readLine()) != null){
                log.info(runString);
            }
            runBufferedReader.close();
            runInputStreamReader.close();
            runInputStream.close();
            runProcess.destroy();
            log.info("recording is done.");

            doRecordFile.delete();

        }catch(IOException e){
            log.error("cannot run do-record.sh: {} {}", e.getMessage(), e);
        }
    }
}
