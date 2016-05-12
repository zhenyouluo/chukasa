package pro.hirooka.chukasa.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import pro.hirooka.chukasa.configuration.SystemConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Slf4j
public class ChukasaRemoverRunner implements Runnable {

    static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private String streamRootPath;

    private final SystemConfiguration systemConfiguration;

    private UUID uuid;

    @Autowired
    public ChukasaRemoverRunner(String streamRootPath, SystemConfiguration systemConfiguration, UUID uuid) {
        this.streamRootPath = streamRootPath;
        this.systemConfiguration = systemConfiguration;
        this.uuid = uuid;
    }

    @Override
    public void run() {

        requireNonNull(systemConfiguration, "systemConfiguration");
        String tempPath = systemConfiguration.getTempPath() + FILE_SEPARATOR + uuid.toString();

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
