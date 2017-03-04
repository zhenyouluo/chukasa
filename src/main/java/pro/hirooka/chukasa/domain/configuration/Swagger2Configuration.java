package pro.hirooka.chukasa.domain.configuration;

import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class Swagger2Configuration {

    @Bean
    public Docket api() {
        Contact contact = new Contact(
                "",
                "",
                ""
        );
        ApiInfo apiInfo = new ApiInfo(
                "chukasa",
                "chukasa API",
                "1",
                "",
                contact,
                "",
                ""
        );
        return new Docket(
                DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                //.paths(PathSelectors.any())
                .paths(Predicates.and(Predicates.containsPattern("/api/*")))
                .build()
                .apiInfo(apiInfo);
    }
}


