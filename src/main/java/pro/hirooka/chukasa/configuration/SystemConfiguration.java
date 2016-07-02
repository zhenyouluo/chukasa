package pro.hirooka.chukasa.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "system")
public class SystemConfiguration {
    String ffmpegPath;
    String recpt1Path;
    String webCameraDeviceName;
    int webCameraAudioChannel;
    String temporaryPath;
    String filePath;
    int ffmpegThreads;
    boolean quickSyncVideoEnabled;
    boolean openmaxEnabled;
}