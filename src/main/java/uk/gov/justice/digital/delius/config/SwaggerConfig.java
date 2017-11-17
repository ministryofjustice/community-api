package uk.gov.justice.digital.delius.config;

import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;

import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket offenderApi() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
            .useDefaultResponseMessages(false)
            .apiInfo(apiInfo())
            .select()
            .apis(RequestHandlerSelectors.any())
            .paths(Predicates.or(regex("(\\/info.*)"),
                regex("(\\/health.*)"),
                regex("(\\/logon.*)"),
                regex("(\\/offenders.*)")))
            .build();

        docket.genericModelSubstitutes(Optional.class);
        docket.directModelSubstitute(ZonedDateTime.class, java.util.Date.class);
        docket.directModelSubstitute(LocalDateTime.class, java.util.Date.class);

        return docket;
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
            "Delius API Documentation",
            "REST service for accessing the Delius Oracle database.",
            "0.0.1", "", contactInfo(), "", "",
            Collections.emptyList());
    }

    private Contact contactInfo() {
        return new Contact(
            "API Accelerator Team",
            "",
            "api-accelerator-gro*up@digital.justice.gov.uk");
    }
}
