package uk.gov.justice.digital.delius.config;

import com.google.common.base.Predicates;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ApplicationContext;
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
import java.util.Properties;

import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Autowired
    private ApplicationContext applicationContext;

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
                        regex("(\\/offenders.*)"),
                        regex("(\\/offenderDeltaIds.*)")))
                .build();

        docket.genericModelSubstitutes(Optional.class);
        docket.directModelSubstitute(ZonedDateTime.class, java.util.Date.class);
        docket.directModelSubstitute(LocalDateTime.class, java.util.Date.class);

        return docket;
    }

    private ApiInfo apiInfo() {

        BuildProperties buildProperties;
        try {
            buildProperties = (BuildProperties) applicationContext.getBean("buildProperties");
        } catch (BeansException be) {
            Properties properties = new Properties();
            properties.put("version", "?");
            buildProperties = new BuildProperties(properties);
        }

        return new ApiInfo(
                "New Nomis API Documentation",
                "REST service for accessing the Nomis Oracle database.",
                buildProperties.getVersion(), "", contactInfo(), "", "",
                Collections.emptyList());
    }

    private Contact contactInfo() {
        return new Contact(
                "API Accelerator Team",
                "",
                "api-accelerator-gro*up@digital.justice.gov.uk");
    }
}
