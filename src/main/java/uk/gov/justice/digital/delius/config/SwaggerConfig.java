package uk.gov.justice.digital.delius.config;

import io.swagger.util.ReferenceSerializationConfigurer;
import lombok.AllArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Response;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.json.JacksonModuleRegistrar;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
    public Docket communityAPI() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .groupName("Community API")
                .tags(new Tag("-- Popular core APIs --", "Endpoints that are most often used by clients"),
                        new Tag("Convictions", "Offender conviction related, also known as Events in Delius"),
                        new Tag("Contact and attendance", "Offender case notes, also known as Contacts in Delius and attendances"),
                        new Tag("Custody", "Offender custody information"),
                        new Tag("Offender managers", "Community and prison offender managers and responsible officers"),
                        new Tag("Documents", "Documents related to a offender and their sentence"),
                        new Tag("Sentence requirements and breach", "Offender sentence requires and breaches of those requirements"),
                        new Tag("Personal circumstances", "Offender personal circumstances, e.g. medical condition"),
                        new Tag("Registrations", "Offender registers, e.g sex offenders register, TACT, MAPPA etc"),
                        new Tag("Authentication and users", "Authentication and authorisation endpoints for access roles and user details"),
                        new Tag("Court appearances", "Offender court appearances that have been recorded by probation"),
                        new Tag("Staff", "Staff related endpoints. Staff are typically offender managers and maybe users of Delius"),
                        new Tag("Events", "Endpoints to support propagation of offender related events"),
                        new Tag("Reference data", "Retrieves Delius reference data"),
                        new Tag("Sentence dates", "Endpoints and set a retrieve sentence dates"),
                        new Tag("Core offender", "Offender related endpoints")
                )
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(
                        regex("(\\/ping.*)")
                                .or(regex("(\\/info.*)"))
                                .or(regex("(\\/health.*)"))
                                .or(regex("(\\/secure/.*)")))
                .build()
                .genericModelSubstitutes(Optional.class)
                .directModelSubstitute(ZonedDateTime.class, java.util.Date.class)
                .directModelSubstitute(LocalDateTime.class, java.util.Date.class)
                .globalResponses(HttpMethod.GET, getCustomizedResponseMessages())
                .globalResponses(HttpMethod.PUT, getCustomizedResponseMessages())
                .globalResponses(HttpMethod.POST, getCustomizedResponseMessages())
                .globalResponses(HttpMethod.DELETE, getCustomizedResponseMessages())
                .forCodeGeneration(true);
    }

    @Bean
    public Docket newTechAPI() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .groupName("NewTech Private APIs")
                .apiInfo(newTechApiInfo())
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(
                        regex("(\\/ping.*)")
                                .or(regex("(\\/info.*)"))
                                .or(regex("(\\/health.*)"))
                                .or(regex("(\\/api/.*)")))
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
                "<h2>REST service for accessing community probation information</h2>" +
                        "<p>This service provides endpoints for accessing data primary sourced from National Delius about people that are of interest to HM Probation Service.</p>" +
                        "<p>There is cross-over with the <b>prison-api</b> though suspects on remand will not be surfaced by this API unless that have previously been on probation.</p>" +
                        "<div>" +
                        "This service is secured by <b>OAuth2</b> with tokens supplied by HMPPS Auth. Most read-only endpoints require the <b>ROLE_COMMUNITY</b> to access, but check each endpoint where this differs." +
                        "<p>This service can be accessed in a number environments. For each environment a different set of OAuth2 credentials from HMPPS Auth are required</p>" +
                        "<ul>" +
                        "<li>Test/Development: <b>https://community-api-secure.test.delius.probation.hmpps.dsd.io</b></li>" +
                        "<li>Pre-production: <b>https://community-api-secure.pre-prod.delius.probation.hmpps.dsd.io</b></li>" +
                        "<li>Production: <b>https://community-api-secure.probation.service.justice.gov.uk</b></li>" +
                        "</ul>" +
                        "<div>",
                buildProperties.getVersion(),
                "https://gateway.nomis-api.service.justice.gov.uk/auth/terms",
                contactInfo(),
                "Open Government Licence v3.0", "https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/", List
                .of());
    }

    private ApiInfo newTechApiInfo() {
        return new ApiInfo(
                "New Tech Private API Documentation",
                "REST service for Probation API as used by New Tech. These APIs are not be available for clients other than NewTech. Please select Community API from the <b>Select a definition</b> dropdown.",
                buildProperties.getVersion(),
                "https://gateway.nomis-api.service.justice.gov.uk/auth/terms",
                contactInfo(),
                "Open Government Licence v3.0", "https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/", List
                .of());
    }

    private List<Response> getCustomizedResponseMessages() {
        List<Response> responseMessages = new ArrayList<>();
        responseMessages.add(new Response("401", "JWT supplied invalid or absent", true, List.of(), List.of(), List.of(), List
                .of()));
        responseMessages.add(new Response("403", "Requires role ROLE_COMMUNITY", true, List
                .of(), List.of(), List.of(), List.of()));
        return responseMessages;
    }

    @Bean
    public JacksonModuleRegistrar swaggerJacksonModuleRegistrar() {
        return ReferenceSerializationConfigurer::serializeAsComputedRef;
    }
}
