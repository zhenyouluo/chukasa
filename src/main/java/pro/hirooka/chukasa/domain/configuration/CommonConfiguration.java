package pro.hirooka.chukasa.domain.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "common")
public class CommonConfiguration {
    String tuner;
}
