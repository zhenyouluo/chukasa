package pro.hirooka.chukasa.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "chukasa")
public class ChukasaConfiguration {
    String[] videoFileExtension;
    long recorderStartMargin;
    long recorderStopMargin;
    String alternativeHlsPlayer;
    String epgdumpExecuteScheduleCron;
    long epgdumpExecuteOnBootIgnoreInterval;
}
