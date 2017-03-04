package pro.hirooka.chukasa.domain.service.chukasa.eraser;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import pro.hirooka.chukasa.domain.configuration.SystemConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static pro.hirooka.chukasa.domain.model.chukasa.constants.ChukasaConstant.FILE_SEPARATOR;

@Slf4j
public class ChukasaRemoverRunner implements Runnable {

    private String streamRootPath;

    private SystemConfiguration systemConfiguration;

    private UUID uuid;

    public ChukasaRemoverRunner(SystemConfiguration systemConfiguration, String streamRootPath, UUID uuid) {
        this.systemConfiguration = requireNonNull(systemConfiguration, "systemConfiguration");
        this.streamRootPath = streamRootPath;
        this.uuid = uuid;
    }

    @Override
    public void run() {

        requireNonNull(systemConfiguration, "systemConfiguration");
        String tempPath = systemConfiguration.getTemporaryPath() + FILE_SEPARATOR + uuid.toString();

        log.info("remove command is called.");
        log.info("streamRootPath: {} and tempPath: {} are to be removed.", streamRootPath, tempPath);

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
}
