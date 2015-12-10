package pro.hirooka.chukasa.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "chukasa")
public class ChukasaConfiguration {
    String streamRootPathName;
    String livePathName;
    String streamFileNamePrefix;
    String m3u8PlaylistName;
    boolean recorderEnabled;
    int epgDays;
    int epgSpan;
    String epgBaseUrl;
}
