package pro.hirooka.chukasa.transcoder;

import lombok.extern.slf4j.Slf4j;
import pro.hirooka.chukasa.ChukasaConstant;
import pro.hirooka.chukasa.domain.chukasa.ChukasaModel;
import pro.hirooka.chukasa.domain.chukasa.type.StreamingType;
import pro.hirooka.chukasa.encrypter.Encrypter;
import pro.hirooka.chukasa.segmenter.SegmenterRunner;
import pro.hirooka.chukasa.service.chukasa.IChukasaModelManagementComponent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

import static java.util.Objects.requireNonNull;

@Slf4j
public class FFmpegRunner implements Runnable {

    static final String FILE_SEPARATOR = System.getProperty("file.separator");

    final String STREAM_FILE_NAME_PREFIX = ChukasaConstant.STREAM_FILE_NAME_PREFIX;
    final String STREAM_FILE_EXTENSION = ChukasaConstant.STREAM_FILE_EXTENSION;

    private int adaptiveBitrateStreaming;

    private IChukasaModelManagementComponent chukasaModelManagementComponent;

    public FFmpegRunner(int adaptiveBitrateStreaming, IChukasaModelManagementComponent chukasaModelManagementComponent){
        this.adaptiveBitrateStreaming = adaptiveBitrateStreaming;
        this.chukasaModelManagementComponent = requireNonNull(chukasaModelManagementComponent, "chukasaModelManagementComponent");
    }

    @Override
    public void run() {

        ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);

        boolean isQSV = chukasaModel.getSystemConfiguration().isQuickSyncVideoEnabled();
        boolean isOpenMAX = chukasaModel.getSystemConfiguration().isOpenmaxEnabled();

        int seqCapturedTimeShifted = chukasaModel.getSeqTsOkkake();

        String[] cmdArray = null;

        if(chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.WEB_CAMERA) {

            if(isQSV){
                String[] cmdArrayTemporary = {

                        chukasaModel.getSystemConfiguration().getFfmpegPath(),
                        "-f", "video4linux2",
                        "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                        "-i", chukasaModel.getSystemConfiguration().getWebCameraDeviceName(),
                        "-f", "alsa",
                        "-ac", Integer.toString(chukasaModel.getSystemConfiguration().getWebCameraAudioChannel()),
                        "-i", "hw:0,0",
                        "-acodec", "aac",
                        "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                        "-ar", "44100",
                        "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                        "-vcodec", "h264_qsv",
                        "-profile:v", "high",
                        "-level", "4.1",
                        "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                        "-pix_fmt", "yuv420p",
                        "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                        "-t", Integer.toString(chukasaModel.getChukasaSettings().getTotalWebCameraLiveduration()),
                        "-f", "mpegts",
                        "-y", chukasaModel.getSystemConfiguration().getTempPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + chukasaModel.getChukasaSettings().getVideoBitrate() + STREAM_FILE_EXTENSION
                };
                cmdArray = cmdArrayTemporary;
            }else{
                String[] cmdArrayTemporary = {

                        chukasaModel.getSystemConfiguration().getFfmpegPath(),
                        "-f", "video4linux2",
                        "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                        //"-r", "30",
                        "-i", chukasaModel.getSystemConfiguration().getWebCameraDeviceName(),
                        "-f", "alsa",
                        "-ac", Integer.toString(chukasaModel.getSystemConfiguration().getWebCameraAudioChannel()),
                        "-i", "hw:0,0",
                        "-acodec", "aac",
                        "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                        "-ar", "44100",
                        "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                        "-vcodec", "libx264",
                        "-profile:v", "high",
                        "-level", "4.1",
                        "-preset:v", "superfast",
                        "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                        "-pix_fmt", "yuv420p",
                        "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                        "-t", Integer.toString(chukasaModel.getChukasaSettings().getTotalWebCameraLiveduration()),
                        "-f", "mpegts",
                        "-x264opts", "keyint=10:min-keyint=10",
                        "-y", chukasaModel.getSystemConfiguration().getTempPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + chukasaModel.getChukasaSettings().getVideoBitrate() + STREAM_FILE_EXTENSION
                };
                cmdArray = cmdArrayTemporary;
            }

        }else if(chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.FILE){

            if(isQSV && !isOpenMAX || isQSV) {
                String[] cmdArrayTemporary = {

                        chukasaModel.getSystemConfiguration().getFfmpegPath(),
                        "-i", chukasaModel.getSystemConfiguration().getFilePath() + FILE_SEPARATOR + chukasaModel.getChukasaSettings().getFileName(),
                        "-acodec", "aac",
                        "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                        "-ac", "2",
                        "-ar", "44100",
                        "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                        "-vcodec", "h264_qsv",
                        "-profile:v", "high",
                        "-level", "4.1",
                        "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                        "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                        "-f", "mpegts",
                        "-y", chukasaModel.getSystemConfiguration().getTempPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + chukasaModel.getChukasaSettings().getVideoBitrate() + STREAM_FILE_EXTENSION
                };
                cmdArray = cmdArrayTemporary;
            }else if(!isQSV && isOpenMAX){
                String[] cmdArrayTemporary = {

                        chukasaModel.getSystemConfiguration().getFfmpegPath(),
                        "-i", chukasaModel.getSystemConfiguration().getFilePath() + FILE_SEPARATOR + chukasaModel.getChukasaSettings().getFileName(),
                        "-acodec", "aac",
                        "-strict", "experimental",
                        "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                        "-ac", "2",
                        "-ar", "44100",
                        //"-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                        "-vcodec", "h264_omx",
                        "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                        "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                        "-f", "mpegts",
                        "-y", chukasaModel.getSystemConfiguration().getTempPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + chukasaModel.getChukasaSettings().getVideoBitrate() + STREAM_FILE_EXTENSION
                };
                cmdArray = cmdArrayTemporary;
            }else{
                String[] cmdArrayTemporary = {

                        chukasaModel.getSystemConfiguration().getFfmpegPath(),
                        "-i", chukasaModel.getSystemConfiguration().getFilePath() + FILE_SEPARATOR + chukasaModel.getChukasaSettings().getFileName(),
                        "-acodec", "aac",
                        "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                        "-ac", "2",
                        "-ar", "44100",
                        "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                        "-vcodec", "libx264",
                        "-profile:v", "high",
                        "-level", "4.1",
                        "-preset:v", "superfast",
                        "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                        "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                        "-f", "mpegts",
                        "-x264opts", "keyint=10:min-keyint=10",
                        "-y", chukasaModel.getSystemConfiguration().getTempPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + chukasaModel.getChukasaSettings().getVideoBitrate() + STREAM_FILE_EXTENSION
                };
                cmdArray = cmdArrayTemporary;
            }

        }else if(chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.OKKAKE){

            if(chukasaModel.getChukasaSettings().isEncrypted()){

                if(isQSV){
                    String[] cmdArrayTemporary = {

                            chukasaModel.getSystemConfiguration().getFfmpegPath(),
                            "-i", chukasaModel.getTempEncPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + seqCapturedTimeShifted + STREAM_FILE_EXTENSION,
                            "-acodec", "aac",
                            "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                            "-ac", "2",
                            "-ar", "44100",
                            "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                            "-vcodec", "h264_qsv",
                            "-profile:v", "high",
                            "-level", "4.1",
                            "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                            "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                            "-f", "mpegts",
                            "-y", chukasaModel.getTempEncPath() + FILE_SEPARATOR + "fileSequenceEncoded" + seqCapturedTimeShifted + STREAM_FILE_EXTENSION // TODO
                    };
                    cmdArray = cmdArrayTemporary;
                }else{
                    String[] cmdArrayTemporary = {

                            chukasaModel.getSystemConfiguration().getFfmpegPath(),
                            "-i", chukasaModel.getTempEncPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + seqCapturedTimeShifted + STREAM_FILE_EXTENSION,
                            "-acodec", "aac",
                            "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                            "-ac", "2",
                            "-ar", "44100",
                            "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                            "-vcodec", "libx264",
                            "-profile:v", "high",
                            "-level", "4.1",
                            "-preset:v", "superfast",
                            "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                            "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                            "-f", "mpegts",
                            "-x264opts", "keyint=10:min-keyint=10",
                            "-y", chukasaModel.getTempEncPath() + FILE_SEPARATOR + "fileSequenceEncoded" + seqCapturedTimeShifted + STREAM_FILE_EXTENSION // TODO
                    };
                    cmdArray = cmdArrayTemporary;
                }

            }else{

                // TODO:

            }

        }

        String cmd = "";
        for(int i = 0; i < cmdArray.length; i++){
            cmd += cmdArray[i] + " ";
        }
        log.info("{}", cmd);

        ProcessBuilder pb = new ProcessBuilder(cmdArray);
        try {

            log.info("Begin FFmpeg");
            Process pr = pb.start();
            InputStream is = pr.getErrorStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            long pid = -1;
            try {
                if (pr.getClass().getName().equals("java.lang.UNIXProcess")) {
                    Field field = pr.getClass().getDeclaredField("pid");
                    field.setAccessible(true);
                    pid = field.getLong(pr);
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
            while((str = br.readLine()) != null){
                log.info(str);
                // TODO Input/output error (in use...)
                if(chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.WEB_CAMERA || chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.FILE) {
                    if(str.startsWith("frame=")){
                        if(!isTranscoding){
                            isTranscoding = true;
                            chukasaModel.setTrascoding(isTranscoding);
                            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                            if(!isSegmenterStarted) {
                                isSegmenterStarted = true;
                                SegmenterRunner segmenterRunner = new SegmenterRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
                                Thread sThread = new Thread(segmenterRunner, "__SegmenterRunner__");
                                sThread.start();
                                chukasaModel.setSegmenterRunner(segmenterRunner);
                                chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                            }
                        }
                    }
                }
            }
            isTranscoding = false;
            chukasaModel.setTrascoding(isTranscoding);
            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
            isSegmenterStarted = false;
            br.close();
            isr.close();
            is.close();
            pr.destroy();
            log.info("End FFmpeg");
            log.info("{} is completed.", this.getClass().getName());

            if(chukasaModel != null && chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.OKKAKE){
                if(chukasaModel.getChukasaSettings().isEncrypted()){
                    seqCapturedTimeShifted = seqCapturedTimeShifted + 1;
                    chukasaModel.setSeqTsOkkake(seqCapturedTimeShifted);
                    chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

                    Encrypter encrypter = new Encrypter(adaptiveBitrateStreaming, chukasaModelManagementComponent);
                    Thread thread = new Thread(encrypter);
                    thread.start();
                }
            }

        } catch (IOException e) {
            log.error("{} {}", e.getMessage(), e);
        }
    }
}