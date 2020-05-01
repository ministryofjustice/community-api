package uk.gov.justice.digital.delius.config;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import io.swagger.util.ReferenceSerializationConfigurer;
import lombok.AllArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.json.JacksonModuleRegistrar;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2
@AllArgsConstructor
@Import(BeanValidatorPluginsConfiguration.class)
public class SwaggerConfig {
    private final BuildProperties buildProperties;

    @Bean
    public Docket offenderApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(Predicates.or(ImmutableList.of(
                        regex("(\\/ping.*)"),
                        regex("(\\/info.*)"),
                        regex("(\\/health.*)"),
                        regex("(\\/api/.*)"),
                        regex("(\\/secure/.*)"))))
                .build()
                .genericModelSubstitutes(Optional.class)
                .directModelSubstitute(ZonedDateTime.class, java.util.Date.class)
                .directModelSubstitute(LocalDateTime.class, java.util.Date.class)
                .forCodeGeneration(true);
    }

    private Contact contactInfo() {
        return new Contact(
                "HMPPS Digital Studio",
                "",
                "dps-hmpps@digital.justice.gov.uk");
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "Community API Documentation",
                "REST service for accessing community information",
                buildProperties.getVersion(),
                "https://gateway.nomis-api.service.justice.gov.uk/auth/terms",
                contactInfo(),
                "Open Government Licence v3.0", "https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/", List.of());
    }

    @Bean
    public JacksonModuleRegistrar swaggerJacksonModuleRegistrar() {
        return ReferenceSerializationConfigurer::serializeAsComputedRef;
    }
}
