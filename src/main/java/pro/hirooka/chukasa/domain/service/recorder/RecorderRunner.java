package pro.hirooka.chukasa.domain.service.recorder;

import lombok.extern.slf4j.Slf4j;
import pro.hirooka.chukasa.domain.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.model.chukasa.enums.HardwareAccelerationType;
import pro.hirooka.chukasa.domain.model.recorder.ReservedProgram;

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

        int physicalCnannel = reservedProgram.getPhysicalChannel();
        long startRecording = reservedProgram.getStartRecording();
        long stopRecording = reservedProgram.getStopRecording();
//        long duration = reservedProgram.getDurationRecording();
        long duration = reservedProgram.getRecordingDuration();
        long d = duration / 3;
        String title = reservedProgram.getTitle();
        String fileName = reservedProgram.getFileName();

        long now = new Date().getTime();

        // start recording immediately
        // Create do-record.sh (do-record_ch_yyyyMMdd_yyyyMMdd.sh)
        String doRecordFileName = "do-record_" + physicalCnannel + "_" + startRecording + "_" + stopRecording + ".sh";
        try{
            File doRecordFile = new File(systemConfiguration.getFilePath() + FILE_SEPARATOR + doRecordFileName);
            log.info("doRecordFile: {}", doRecordFileName);
            if (!doRecordFile.exists()) {
                doRecordFile.createNewFile();
                BufferedWriter bw = new BufferedWriter(new FileWriter(doRecordFile));
                bw.write("#!/bin/bash");
                bw.newLine();
                bw.write(systemConfiguration.getRecxxxPath() + " --b25 --strip " + physicalCnannel + " " + duration + " \"" + systemConfiguration.getFilePath() + FILE_SEPARATOR + fileName + "\"" + " >/dev/null");
                bw.newLine();
                bw.write(systemConfiguration.getFfmpegPath() +  " -i " + "\"" + systemConfiguration.getFilePath() + FILE_SEPARATOR + fileName + "\"" + " -ss " + d + " -vframes 1 -f image2 " + "\"" + systemConfiguration.getFilePath() + FILE_SEPARATOR + fileName + ".jpg\"" + " >/dev/null");
                bw.newLine();
                if(systemConfiguration.equals(HardwareAccelerationType.H264_QSV)) {
                    bw.write(systemConfiguration.getFfmpegPath() + " -i " + "\"" + systemConfiguration.getFilePath() + FILE_SEPARATOR + fileName + "\"" + " -acodec aac -ab 160k -ar 44100 -ac 2 -s 1280x720 -vcodec h264_qsv -profile:v high -level 4.2 -b:v 2400k -threads 1 -y " + "\"" + systemConfiguration.getFilePath() + FILE_SEPARATOR + fileName + ".m4v\"" + " >/dev/null");
                    bw.newLine();
                    bw.write(systemConfiguration.getFfmpegPath() + " -i " + "\"" + systemConfiguration.getFilePath() + FILE_SEPARATOR + fileName + "\"" + " -acodec aac -ab 32k -ar 44100 -ac 2 -s 320x180 -vcodec h264_qsv -profile:v high -level 4.1 -b:v 160k -threads 1 -y " + "\"" + systemConfiguration.getFilePath() + FILE_SEPARATOR + fileName + ".watch.m4v\"" + " >/dev/null");
                }else{
                    bw.write(systemConfiguration.getFfmpegPath() + " -i " + "\"" + systemConfiguration.getFilePath() + FILE_SEPARATOR + fileName + "\"" + " -acodec aac -ab 160k -ar 44100 -ac 2 -s 1280x720 -vcodec libx264 -profile:v high -level 4.2 -b:v 2400k -threads 1 -y " + "\"" + systemConfiguration.getFilePath() + FILE_SEPARATOR + fileName + ".m4v\"" + " >/dev/null");
                    bw.newLine();
                    bw.write(systemConfiguration.getFfmpegPath() + " -i " + "\"" + systemConfiguration.getFilePath() + FILE_SEPARATOR + fileName + "\"" + " -acodec aac -ab 32k -ar 44100 -ac 2 -s 320x180 -vcodec libx264 -profile:v high -level 4.1 -b:v 160k -threads 1 -y " + "\"" + systemConfiguration.getFilePath() + FILE_SEPARATOR + fileName + ".watch.m4v\"" + " >/dev/null");
                } // TODO: OpenMAX
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

    // TODO:
    HardwareAccelerationType getVideoCodecType() {
        final String H264_QSV = "--enable-libmfx";
        final String H264 = "--enable-x264";
        final String H264_OMX = "--enable-omx-rpi";
        String ffmpeg = systemConfiguration.getFfmpegPath();
        String[] command = {ffmpeg, "-version"};
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            Process process = processBuilder.start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String str = "";
            while ((str = bufferedReader.readLine()) != null) {
                log.info(str);
                if (str.contains(H264_QSV)) {
                    bufferedReader.close();
                    process.destroy();
                    return HardwareAccelerationType.H264_QSV;
                }
                if (str.contains(H264_OMX)) {
                    bufferedReader.close();
                    process.destroy();
                    return HardwareAccelerationType.H264_OMX;
                }
                if (str.contains(H264)) {
                    bufferedReader.close();
                    process.destroy();
                    return HardwareAccelerationType.H264;
                }
            }
            bufferedReader.close();
            process.destroy();
        } catch (IOException e) {
            log.error("{} {}", e.getMessage(), e);
        }
        return HardwareAccelerationType.UNKNOWN;
    }
}
