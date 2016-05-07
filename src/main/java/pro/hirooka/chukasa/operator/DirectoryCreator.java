package pro.hirooka.chukasa.operator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.ChukasaModel;
import pro.hirooka.chukasa.domain.type.StreamingType;
import pro.hirooka.chukasa.service.IChukasaModelManagementComponent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
public class DirectoryCreator implements IDirectoryCreator{

    static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private final IChukasaModelManagementComponent chukasaModelManagementComponent;

    @Autowired
    public DirectoryCreator(IChukasaModelManagementComponent chukasaModelManagementComponent) {
        this.chukasaModelManagementComponent = requireNonNull(chukasaModelManagementComponent, "chukasaModelManagementComponent");
    }

    @Override
    public void setup(int adaptiveBitrateStreaming) {

        // directory
        // streamRootPath ... from Context
        // streamPath ... streamRootPath + ChukasaConfiguration
        // temporaryPath ... SystemConfiguration
        // temporaryEncpryterPath ... tempPath + ChukasaConfiguration
        // filePath ... SystemConfiguration

        ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);

        // create temporary direcotory
        String temporaryPath = chukasaModel.getSystemConfiguration().getTempPath();
        temporaryPath = temporaryPath + FILE_SEPARATOR + chukasaModel.getUuid().toString();
        if (new File(temporaryPath).mkdirs()) {
            log.info("temporaryPath is created : {}", temporaryPath);
        } else {
            log.error("temporaryPath cannot be created {}", temporaryPath);
        }

        // create directory to deploy segmented MPEG2-TS files
        String streamRootPath = chukasaModel.getStreamRootPath();
        log.info("streamRootPath: {}", streamRootPath);
        chukasaModel.setStreamRootPath(streamRootPath);

        if (new File(streamRootPath).mkdirs()) {
            log.info("streamRootPath is created : {}", streamRootPath);
        } else {
            log.error("streamRootPath cannot be created {}", streamRootPath);
        }

        // create directory to deploy segmented MPEG2-TS files (under streamRootPath)
        String streamPath = "";
        String tempEncPath = "";
        String tempPath = chukasaModel.getSystemConfiguration().getTempPath();
        if (chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.WEB_CAMERA || chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.CAPTURE) {
            streamPath = streamRootPath + FILE_SEPARATOR + chukasaModel.getChukasaConfiguration().getLivePathName();
            chukasaModel.setStreamPath(streamPath);
            tempEncPath = tempPath + FILE_SEPARATOR + chukasaModel.getChukasaConfiguration().getLivePathName();
            chukasaModel.setTempEncPath(tempEncPath);
        } else {
            streamPath = streamRootPath + FILE_SEPARATOR + chukasaModel.getChukasaSettings().getFileName();
            chukasaModel.setStreamPath(streamPath);
            tempEncPath = tempPath + FILE_SEPARATOR + chukasaModel.getChukasaSettings().getFileName();
            chukasaModel.setTempEncPath(tempEncPath);
        }

        if (new File(streamPath).mkdirs()) {
            log.info("streamPath is created {}", streamPath);
        } else {
            log.error("streamPath cannot be created {}", streamPath);
        }
        if (new File(tempEncPath).mkdirs()) {
            log.info("tempEncPath is created {}", tempEncPath);
        } else {
            log.error("tempEncPath cannot be created {}", tempEncPath);
        }

        // create directory to deploy segmented MPEG2-TS files (per Video bitrate...)
        streamPath = streamPath + FILE_SEPARATOR + chukasaModel.getChukasaSettings().getVideoBitrate();
        if(Files.exists(new File(streamPath).toPath())){
            try {
                FileUtils.cleanDirectory(new File(streamPath));
                chukasaModel.setStreamPath(streamPath);
                log.info("clean {} as streamPath", streamPath);
            } catch (IOException e) {
                log.error("cannot clean {} as streamPath", streamPath);
            }
        }else{
            if(new File(streamPath).mkdirs()){
                chukasaModel.setStreamPath(streamPath);
                log.info("create {} as streamPath", streamPath);
            }else{
                log.error("cannot create {} as streamPath", streamPath);
            }
        }

        tempEncPath = tempEncPath + FILE_SEPARATOR + chukasaModel.getChukasaSettings().getVideoBitrate();
        if(Files.exists(new File(tempEncPath).toPath())){
            try {
                FileUtils.cleanDirectory(new File(tempEncPath));
                chukasaModel.setTempEncPath(tempEncPath);
                log.info("clean {} as tempEncPath", tempEncPath);
            } catch (IOException e) {
                log.error("cannot clean {} as tempEncPath", tempEncPath);
            }
        }else{
            if(new File(tempEncPath).mkdirs()){
                chukasaModel.setTempEncPath(tempEncPath);
                log.info("create {} as tempEncPath", tempEncPath);
            }else{
                log.error("cannot create {} as tempEncPath", tempEncPath);
            }
        }

    }
}
