package pro.hirooka.chukasa.capture;

import lombok.extern.slf4j.Slf4j;
import pro.hirooka.chukasa.ChukasaConstant;
import pro.hirooka.chukasa.domain.chukasa.ChukasaModel;
import pro.hirooka.chukasa.service.chukasa.IChukasaModelManagementComponent;

import java.io.*;
import java.lang.reflect.Field;

import static java.util.Objects.requireNonNull;

@Slf4j
public class CaptureRunner implements Runnable {

    static final String FILE_SEPARATOR = System.getProperty("file.separator");

    final String STREAM_FILE_NAME_PREFIX = ChukasaConstant.STREAM_FILE_NAME_PREFIX;
    final String STREAM_FILE_EXTENSION = ChukasaConstant.STREAM_FILE_EXTENSION;
    final String FFMPEG_HLS_M3U8_FILE_NAME = ChukasaConstant.FFMPEG_HLS_M3U8_FILE_NAME;
    final String M3U8_FILE_EXTENSION = ChukasaConstant.M3U8_FILE_EXTENSION;

    private int adaptiveBitrateStreaming;

    private IChukasaModelManagementComponent chukasaModelManagementComponent;

    public CaptureRunner(int adaptiveBitrateStreaming, IChukasaModelManagementComponent chukasaModelManagementComponent) {
        this.adaptiveBitrateStreaming = adaptiveBitrateStreaming;
        this.chukasaModelManagementComponent = requireNonNull(chukasaModelManagementComponent, "chukasaModelManagementComponent");
    }

    @Override
    public void run() {

        ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);
        log.debug("StreamPath: {}", chukasaModel.getStreamPath());

        boolean isQSV = chukasaModel.getSystemConfiguration().isQuickSyncVideoEnabled();

        boolean isEncryption = chukasaModel.getChukasaSettings().isEncrypted();
        String ffmpegOutputPath = chukasaModel.getStreamPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + "%d" + STREAM_FILE_EXTENSION;
        if(isEncryption){
            ffmpegOutputPath = chukasaModel.getTempEncPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + "%d" + STREAM_FILE_EXTENSION;;
        }

        String[] commandArray = {""};

        if(isQSV){
            String[] commandArrayTemporary =  {
                    chukasaModel.getSystemConfiguration().getRecpt1Path(),
                    "--b25", "--strip",
                    Integer.toString(chukasaModel.getChukasaSettings().getCh()),
                    "-", "-",
                    "|",
                    chukasaModel.getSystemConfiguration().getFfmpegPath(),
                    "-i", "-",
                    "-acodec", "aac",
                    "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                    "-ar", "44100",
                    "-ac", "2",
                    "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                    "-vcodec", "h264_qsv",
                    "-g", "60",
                    "-profile:v", "high",
                    "-level", "4.2",
                    "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate()+"k",
                    "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                    "-f", "segment",
                    "-segment_format", "mpegts",
                    "-segment_time", Integer.toString(chukasaModel.getHlsConfiguration().getDuration()),
                    "-segment_list", chukasaModel.getStreamPath() + FILE_SEPARATOR + FFMPEG_HLS_M3U8_FILE_NAME + M3U8_FILE_EXTENSION,
                    ffmpegOutputPath
            };
            commandArray = commandArrayTemporary;
        }else{
            String[] commandArrayTemporary = {
                    chukasaModel.getSystemConfiguration().getRecpt1Path(),
                    "--b25", "--strip",
                    Integer.toString(chukasaModel.getChukasaSettings().getCh()),
                    "-", "-",
                    "|",
                    chukasaModel.getSystemConfiguration().getFfmpegPath(),
                    "-i", "-",
                    "-acodec", "aac",
                    "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                    "-ar", "44100",
                    "-ac", "2",
                    "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                    "-vcodec", "libx264",
                    "-profile:v", "high",
                    "-level", "4.1",
                    "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate()+"k",
                    "-preset:v", "superfast",
                    "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                    "-f", "segment",
                    "-segment_format", "mpegts",
                    "-segment_time", Integer.toString(chukasaModel.getHlsConfiguration().getDuration()),
                    "-segment_list", chukasaModel.getStreamPath() + FILE_SEPARATOR + FFMPEG_HLS_M3U8_FILE_NAME + M3U8_FILE_EXTENSION,
                    "-x264opts", "keyint=10:min-keyint=10",
                    ffmpegOutputPath
            };
            commandArray = commandArrayTemporary;
        }

        String command = "";
        for(int i = 0; i < commandArray.length; i++){
            command += commandArray[i] + " ";
        }
        log.info("command = {}", command);

        String captureShell = chukasaModel.getSystemConfiguration().getTemporaryPath() + FILE_SEPARATOR + "capture.sh";
        File file = new File(captureShell);
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write("#!/bin/bash");
            bufferedWriter.newLine();
            bufferedWriter.write(command);
        } catch (IOException e) {
            log.error("{} {}", e.getMessage(), e);
        }

        // chmod 755 capture.sh
        if(true){
            String[] chmodCommandArray = {"chmod", "755", captureShell};
            ProcessBuilder processBuilder = new ProcessBuilder(chmodCommandArray);
            try {
                Process process = processBuilder.start();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String str = "";
                while((str = bufferedReader.readLine()) != null){
                    log.debug("{}", str);
                }
                bufferedReader.close();
                process.destroy();
            } catch (IOException e) {
                log.error("{} {}", e.getMessage(), e);
            }
        }

        // run capture.sh
        if(true){
            String[] capureCommandArray = {captureShell};
            ProcessBuilder processBuilder = new ProcessBuilder(capureCommandArray);
            try {
                Process process = processBuilder.start();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                long pid = -1;
                try {
                    if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
                        Field field = process.getClass().getDeclaredField("pid");
                        field.setAccessible(true);
                        pid = field.getLong(process);
                        chukasaModel.setFfmpegPID(pid);
                        chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                        field.setAccessible(false);
                    }
                } catch (Exception e) {
                    log.error("{} {}", e.getMessage(), e);
                }

                String str = "";
                boolean isTranscoding = false;
                boolean isSegmenterStarted = false;
                while((str = bufferedReader.readLine()) != null){
                    log.debug("{}", str);
                    if(str.startsWith("frame=")){
                        if(!isTranscoding){
                            isTranscoding = true;
                            chukasaModel.setTrascoding(isTranscoding);
                            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
//                            if(!isSegmenterStarted) {
//                                isSegmenterStarted = true;
//                                SegmenterRunner segmenterRunner = new SegmenterRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
//                                Thread sThread = new Thread(segmenterRunner, "__SegmenterRunner__");
//                                sThread.start();
//                                chukasaModel.setSegmenterRunner(segmenterRunner);
//                                chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
//                            }
                        }
                    }
                }
                isTranscoding = false;
                chukasaModel.setTrascoding(isTranscoding);
                chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                bufferedReader.close();
                process.destroy();
            } catch (IOException e) {
                log.error("{} {}", e.getMessage(), e);
            }
        }
    }
}
