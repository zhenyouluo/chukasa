package pro.hirooka.chukasa.handler;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.configuration.SystemConfiguration;

import java.io.File;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
public class ChukasaRemover {

    @Setter
    private String streamRootPath;

    @Autowired
    SystemConfiguration systemConfiguration;

    public void remove(){

        requireNonNull(systemConfiguration, "systemConfiguration");
        String tempPath = systemConfiguration.getTemporaryPath();

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
