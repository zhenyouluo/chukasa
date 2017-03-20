package pro.hirooka.chukasa.api.v1.helper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.model.chukasa.constants.ChukasaConstant;
import pro.hirooka.chukasa.domain.model.chukasa.ChukasaModel;
import pro.hirooka.chukasa.domain.model.chukasa.enums.StreamingType;
import pro.hirooka.chukasa.domain.service.chukasa.IChukasaModelManagementComponent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@Component
public class DirectoryCreator implements IDirectoryCreator{

    static final String FILE_SEPARATOR = System.getProperty("file.separator");

    final String STREAM_ROOT_PATH_NAME = ChukasaConstant.STREAM_ROOT_PATH_NAME;
    final String LIVE_PATH_NAME = ChukasaConstant.LIVE_PATH_NAME;

    @Autowired
    IChukasaModelManagementComponent chukasaModelManagementComponent;

    @Override
    public void setup(int adaptiveBitrateStreaming) {

        // directory
        // streamRootPath ... Context + 定数ファイル
        // streamPath ... streamRootPath + ChukasaConfiguration
        // temporaryPath ... SystemConfiguration
        // temporaryEncpryterPath ... tempPath + ChukasaConfiguration
        // filePath ... SystemConfiguration

        ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);

        // create directory to deploy segmented MPEG2-TS files
        String streamRootPath = chukasaModel.getStreamRootPath() + STREAM_ROOT_PATH_NAME;
        log.info("streamRootPath: {}", streamRootPath);
//        if (new File(streamRootPath).mkdirs()) {
//            log.info("streamRootPath is created : {}", streamRootPath);
//        } else {
//            log.error("streamRootPath cannot be created {}", streamRootPath);
//        }

        // create temporary direcotory
        String temporaryPath = chukasaModel.getSystemConfiguration().getTemporaryPath();
//        if (new File(temporaryPath).mkdirs()) {
//            log.info("temporaryPath is created : {}", temporaryPath);
//        } else {
//            log.error("temporaryPath cannot be created {}", temporaryPath);
//        }

        // create directory to deploy segmented MPEG2-TS files (under streamRootPath)
        String basementStreamPath = "";
        String streamPath = "";
        String temporaryEncryptedStreamPath = "";
        String tempEncPath = "";
        String tempPath = chukasaModel.getSystemConfiguration().getTemporaryPath();
        if(chukasaModel.getChukasaSettings().getStreamingType().equals(StreamingType.TUNER) || chukasaModel.getChukasaSettings().getStreamingType().equals(StreamingType.WEBCAM)){
            basementStreamPath = chukasaModel.getUuid().toString() + FILE_SEPARATOR + chukasaModel.getAdaptiveBitrateStreaming() + FILE_SEPARATOR + chukasaModel.getChukasaSettings().getTranscodingSettings().getName() + FILE_SEPARATOR + LIVE_PATH_NAME;
            streamPath = streamRootPath + FILE_SEPARATOR + basementStreamPath;
            chukasaModel.setStreamPath(streamPath);
            temporaryEncryptedStreamPath = temporaryPath + FILE_SEPARATOR + basementStreamPath;
            chukasaModel.setTempEncPath(temporaryEncryptedStreamPath);
        }else if (chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.WEBCAM) {
            streamPath = streamRootPath + FILE_SEPARATOR + LIVE_PATH_NAME;
            chukasaModel.setStreamPath(streamPath);
            tempEncPath = tempPath + FILE_SEPARATOR + LIVE_PATH_NAME;
            chukasaModel.setTempEncPath(tempEncPath);
        } else {
            basementStreamPath = chukasaModel.getUuid().toString() + FILE_SEPARATOR + chukasaModel.getAdaptiveBitrateStreaming() + FILE_SEPARATOR + chukasaModel.getChukasaSettings().getTranscodingSettings().getName() + FILE_SEPARATOR + chukasaModel.getChukasaSettings().getFileName();
            streamPath = streamRootPath + FILE_SEPARATOR + basementStreamPath;
            chukasaModel.setStreamPath(streamPath);
            temporaryEncryptedStreamPath = temporaryPath + FILE_SEPARATOR + basementStreamPath;
            chukasaModel.setTempEncPath(temporaryEncryptedStreamPath);
        }

//        if (new File(streamPath).mkdirs()) {
//            log.info("streamPath is created {}", streamPath);
//        } else {
//            log.error("streamPath cannot be created {}", streamPath);
//        }
//        if (new File(tempEncPath).mkdirs()) {
//            log.info("tempEncPath is created {}", tempEncPath);
//        } else {
//            log.error("tempEncPath cannot be created {}", tempEncPath);
//        }

        // create directory to deploy segmented MPEG2-TS files (per Video bitrate...)
//        streamPath = streamPath + FILE_SEPARATOR + chukasaModel.getChukasaSettings().getEncodingSettingsType().getName();
        if(Files.exists(new File(streamPath).toPath())){
            try {
                FileUtils.cleanDirectory(new File(streamPath));
//                chukasaModel.setStreamPath(streamPath);
                log.info("clean {} as streamPath", streamPath);
            } catch (IOException e) {
                log.error("cannot clean {} as streamPath", streamPath);
            }
        }else{
            if(new File(streamPath).mkdirs()){
//                chukasaModel.setStreamPath(streamPath);
                log.info("create {} as streamPath", streamPath);
            }else{
                log.error("cannot create {} as streamPath", streamPath);
            }
        }

        tempEncPath = tempEncPath + FILE_SEPARATOR + chukasaModel.getChukasaSettings().getTranscodingSettings().getName();
        if(Files.exists(new File(temporaryEncryptedStreamPath).toPath())){
            try {
                FileUtils.cleanDirectory(new File(temporaryEncryptedStreamPath));
//                chukasaModel.setTempEncPath(tempEncPath);
                log.info("clean {} as tempEncPath", tempEncPath);
            } catch (IOException e) {
                log.error("cannot clean {} as tempEncPath", tempEncPath);
            }
        }else{
            if(new File(temporaryEncryptedStreamPath).mkdirs()){
//                chukasaModel.setTempEncPath(tempEncPath);
                log.info("create {} as tempEncPath", tempEncPath);
            }else{
                log.error("cannot create {} as tempEncPath", tempEncPath);
            }
        }

        chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
    }
}
