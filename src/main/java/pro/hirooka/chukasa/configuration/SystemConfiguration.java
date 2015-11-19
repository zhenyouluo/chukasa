package pro.hirooka.chukasa.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "system")
public class SystemConfiguration {
    String usbCameraFfmpegPath;
    String fileFfmpegPath;
    String captureFfmpegPath;
    String captureProgramPath;
    String usbCameraDeviceName;
    String tempPath;
    String filePath;
    int ffmpegThreads;
}