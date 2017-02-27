package pro.hirooka.chukasa.api.v1.helper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import pro.hirooka.chukasa.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.model.chukasa.ChukasaModel;
import pro.hirooka.chukasa.domain.model.chukasa.enums.StreamingType;
import pro.hirooka.chukasa.domain.model.chukasa.enums.VideoCodecType;
import pro.hirooka.chukasa.domain.service.chukasa.IChukasaModelManagementComponent;
import pro.hirooka.chukasa.domain.service.chukasa.transcoder.FFmpegInitializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static pro.hirooka.chukasa.domain.model.chukasa.constants.ChukasaConstant.*;

@Slf4j
public class ChukasaUtility {

    public static void initializeRunner(IChukasaModelManagementComponent chukasaModelManagementComponent, SystemConfiguration systemConfiguration){

        for(ChukasaModel chukasaModel : chukasaModelManagementComponent.get()){

            if(chukasaModel.getSegmenterRunner() != null){
                chukasaModel.getSegmenterRunner().stop();
            }
            if(chukasaModel.getPlaylisterRunner() != null){
                chukasaModel.getPlaylisterRunner().stop();
            }
            if(chukasaModel.getFfmpegHLSStreamDetectorRunner() != null){
                chukasaModel.getFfmpegHLSStreamDetectorRunner().stop();
            }
            if(chukasaModel.getFfmpegHLSEncrypterRunner() != null){
                chukasaModel.getFfmpegHLSEncrypterRunner().stop();
            }

            SimpleAsyncTaskExecutor ffmpegInitializerSimpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
            FFmpegInitializer ffmpegInitializer = new FFmpegInitializer(chukasaModel.getFfmpegPID());
            ffmpegInitializerSimpleAsyncTaskExecutor.execute(ffmpegInitializer);

//            SimpleAsyncTaskExecutor chukasaRemoverRunnerSimpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
//            ChukasaRemoverRunner chukasaRemoverRunner = new ChukasaRemoverRunner(systemConfiguration, chukasaModel.getStreamRootPath(), chukasaModel.getUuid());
//            chukasaRemoverRunnerSimpleAsyncTaskExecutor.execute(chukasaRemoverRunner);

            String streamRootPath = chukasaModel.getStreamRootPath();
            String tempPath = systemConfiguration.getTemporaryPath() + FILE_SEPARATOR + chukasaModel.getUuid().toString();
            try {
                FileUtils.cleanDirectory(new File(streamRootPath));
                FileUtils.cleanDirectory(new File(tempPath));
                if((new File(streamRootPath)).delete() && (new File(tempPath)).delete()){
                    log.info("all Chukasa files have been removed completely.");
                }else{
                    log.warn("all Chukasa files have not been removed completely.");
                }
            } catch (IOException e) {
                log.error("{} {}", e.getMessage(), e);
            }
        }
        chukasaModelManagementComponent.deleteAll();
    }

    public static ChukasaModel operateEncodingSettings(ChukasaModel chukasaModel){
        String encodingSettings = chukasaModel.getChukasaSettings().getEncodingSettingsType().getName();
        try {
            String videoResolution = encodingSettings.split("-")[0];
            int videoBitrate = Integer.parseInt(encodingSettings.split("-")[1]);
            int audioBitrate = Integer.parseInt(encodingSettings.split("-")[2]);
            chukasaModel.getChukasaSettings().setVideoResolution(videoResolution);
            chukasaModel.getChukasaSettings().setVideoBitrate(videoBitrate);
            chukasaModel.getChukasaSettings().setAudioBitrate(audioBitrate);
            return chukasaModel;
        }catch (NumberFormatException e){
            return null;
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public static ChukasaModel createChukasaDerectory(ChukasaModel chukasaModel){

        String streamRootPath = chukasaModel.getStreamRootPath();
        String temporaryPath = chukasaModel.getSystemConfiguration().getTemporaryPath();

        String basementStreamPath = "";
        String streamPath = "";
        String temporaryEncryptedStreamPath = "";
        if(chukasaModel.getChukasaSettings().getStreamingType().equals(StreamingType.CAPTURE) || chukasaModel.getChukasaSettings().getStreamingType().equals(StreamingType.WEB_CAMERA)){
            basementStreamPath = chukasaModel.getUuid().toString() + FILE_SEPARATOR + chukasaModel.getAdaptiveBitrateStreaming() + FILE_SEPARATOR + chukasaModel.getChukasaSettings().getEncodingSettingsType().getName() + FILE_SEPARATOR + LIVE_PATH_NAME;
            streamPath = streamRootPath + FILE_SEPARATOR + basementStreamPath;
            chukasaModel.setStreamPath(streamPath);
            temporaryEncryptedStreamPath = temporaryPath + FILE_SEPARATOR + basementStreamPath;
            chukasaModel.setTempEncPath(temporaryEncryptedStreamPath);
        }else if (chukasaModel.getChukasaSettings().getStreamingType().equals(StreamingType.WEB_CAMERA)) {
            streamPath = streamRootPath + FILE_SEPARATOR + LIVE_PATH_NAME;
            chukasaModel.setStreamPath(streamPath);
            temporaryEncryptedStreamPath = temporaryPath + FILE_SEPARATOR + LIVE_PATH_NAME;
            chukasaModel.setTempEncPath(temporaryEncryptedStreamPath);
        } else {
            basementStreamPath = chukasaModel.getUuid().toString() + FILE_SEPARATOR + chukasaModel.getAdaptiveBitrateStreaming() + FILE_SEPARATOR + chukasaModel.getChukasaSettings().getEncodingSettingsType().getName() + FILE_SEPARATOR + chukasaModel.getChukasaSettings().getFileName();
            streamPath = streamRootPath + FILE_SEPARATOR + basementStreamPath;
            chukasaModel.setStreamPath(streamPath);
            temporaryEncryptedStreamPath = temporaryPath + FILE_SEPARATOR + basementStreamPath;
            chukasaModel.setTempEncPath(temporaryEncryptedStreamPath);
        }


        // create directory to deploy segmented MPEG2-TS files (per Video bitrate...)
        if(Files.exists(new File(streamPath).toPath())){
            try {
                FileUtils.cleanDirectory(new File(streamPath));
                log.info("clean {} as streamPath", streamPath);
            } catch (IOException e) {
                log.error("cannot clean {} as streamPath", streamPath);
                return null;
            }
        }else{
            if(new File(streamPath).mkdirs()){
                log.info("create {} as streamPath", streamPath);
            }else{
                log.error("cannot create {} as streamPath", streamPath);
            }
        }

        if(Files.exists(new File(temporaryEncryptedStreamPath).toPath())){
            try {
                FileUtils.cleanDirectory(new File(temporaryEncryptedStreamPath));
                log.info("clean {} as tempEncPath", temporaryEncryptedStreamPath);
            } catch (IOException e) {
                log.error("cannot clean {} as tempEncPath", temporaryEncryptedStreamPath);
            }
        }else{
            if(new File(temporaryEncryptedStreamPath).mkdirs()){
                log.info("create {} as tempEncPath", temporaryEncryptedStreamPath);
            }else{
                log.error("cannot create {} as tempEncPath", temporaryEncryptedStreamPath);
            }
        }

        return chukasaModel;
    }

    public static ChukasaModel calculateTimerTaskParameter(ChukasaModel chukasaModel) {

        // segmenter timer parameters [ms]
        int duration = chukasaModel.getHlsConfiguration().getDuration();
        int uriInPlaylist = chukasaModel.getHlsConfiguration().getUriInPlaylist();

        long timerSegmenterDelay = (long) (duration * 1000 * (uriInPlaylist - 1));
        StreamingType streamingType = chukasaModel.getChukasaSettings().getStreamingType();
        if (streamingType.equals(StreamingType.FILE)
                || streamingType.equals(StreamingType.CAPTURE)
                || streamingType.equals(StreamingType.WEB_CAMERA)) {
            timerSegmenterDelay = (long) (duration * 1000);
        }
        if(chukasaModel.getVideoCodecType().equals(VideoCodecType.H264_OMX)){
            timerSegmenterDelay = timerSegmenterDelay + 3000; // todo
        }
        long timerSegmenterPeriod = (long) (duration * 1000);

        // playlister timer parameters [ms]
        long timerPlaylisterDelay = 0;
        long timerPlaylisterPeriod = (long) (duration * 1000);

        log.info("timerSegmenterDelay = {}, timerSegmenterPeriod = {}, timerPlaylisterDelay = {}, timerPlaylisterPeriod = {}", timerSegmenterDelay, timerSegmenterPeriod, timerPlaylisterDelay, timerPlaylisterPeriod);

        chukasaModel.setTimerSegmenterDelay(timerSegmenterDelay);
        chukasaModel.setTimerSegmenterPeriod(timerSegmenterPeriod);
        chukasaModel.setTimerPlaylisterDelay(timerPlaylisterDelay);
        chukasaModel.setTimerPlaylisterPeriod(timerPlaylisterPeriod);
        return chukasaModel;
    }

    public static String buildM3u8URI(ChukasaModel chukasaModel){
        String m3u8URI = "/";
        if(chukasaModel.getChukasaSettings().getStreamingType().equals(StreamingType.WEB_CAMERA)
                || chukasaModel.getChukasaSettings().getStreamingType().equals(StreamingType.CAPTURE)){
            m3u8URI = "/"
                    + STREAM_ROOT_PATH_NAME
                    + FILE_SEPARATOR
                    + chukasaModel.getUuid().toString()
                    + FILE_SEPARATOR
                    + chukasaModel.getAdaptiveBitrateStreaming()
                    + FILE_SEPARATOR
                    + chukasaModel.getChukasaSettings().getEncodingSettingsType().getName()
                    + FILE_SEPARATOR
                    + LIVE_PATH_NAME
                    + FILE_SEPARATOR
                    + M3U8_FILE_NAME + M3U8_FILE_EXTENSION;
        }else if(chukasaModel.getChukasaSettings().getStreamingType().equals(StreamingType.FILE)
                || chukasaModel.getChukasaSettings().getStreamingType().equals(StreamingType.OKKAKE)){
            m3u8URI = "/"
                    + STREAM_ROOT_PATH_NAME
                    + FILE_SEPARATOR
                    + chukasaModel.getUuid().toString()
                    + FILE_SEPARATOR
                    + chukasaModel.getAdaptiveBitrateStreaming()
                    + FILE_SEPARATOR
                    + chukasaModel.getChukasaSettings().getEncodingSettingsType().getName()
                    + FILE_SEPARATOR
                    + chukasaModel.getChukasaSettings().getFileName()
                    + FILE_SEPARATOR
                    + M3U8_FILE_NAME + M3U8_FILE_EXTENSION;
        }
        log.info(m3u8URI);
        return m3u8URI;
    }
}
