package pro.hirooka.chukasa.domain.configuration;

import org.springframework.boot.web.server.MimeMappings;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebServerConfiguration implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    @Override
    public void customize(ConfigurableServletWebServerFactory server) {
        MimeMappings mimeMappings = new MimeMappings(MimeMappings.DEFAULT);
        mimeMappings.add("m3u8", "application/x-mpegURL");
        mimeMappings.add("ts", "video/MP2T");
        mimeMappings.add("m4v", "video/x-m4v");
        server.setMimeMappings(mimeMappings);
    }
}
