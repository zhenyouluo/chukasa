package pro.hirooka.chukasa.domain.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "hls")
public class HLSConfiguration {
    int duration;
    int uriInPlaylist;
}
