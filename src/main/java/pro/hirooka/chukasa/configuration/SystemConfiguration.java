package pro.hirooka.chukasa.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "system")
public class SystemConfiguration {
    String webCameraFfmpegPath;
    String fileFfmpegPath;
    String captureFfmpegPath;
    String captureProgramPath;
    String webCameraDeviceName;
    String tempPath;
    String filePath;
    int ffmpegThreads;
}