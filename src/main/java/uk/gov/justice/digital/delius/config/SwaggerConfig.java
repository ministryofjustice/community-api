package uk.gov.justice.digital.delius.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.util.List;

@OpenAPIDefinition(
    info = @Info(
        title = "Community API Documentation",
        description = """
            <h2>REST service for accessing community probation information</h2>
            <p>This service provides endpoints for accessing data primary sourced from National Delius about people that are of interest to HM Probation Service.</p>
            <p>There is cross-over with the <b>prison-api</b> though suspects on remand will not be surfaced by this API unless that have previously been on probation.</p>
            <div>
            This service is secured by <b>OAuth2</b> with tokens supplied by HMPPS Auth. Most read-only endpoints require the <b>ROLE_COMMUNITY</b> to access, but check each endpoint where this differs.
            <p>This service can be accessed in a number environments. For each environment a different set of OAuth2 credentials from HMPPS Auth are required</p>
            <ul>
            <li>Development: <b>https://community-api.dev.probation.service.justice.gov.uk</b></li>
            <li>Test: <b>https://community-api.test.probation.service.justice.gov.uk</b></li>
            <li>Pre-production: <b>https://community-api.pre-prod.delius.probation.hmpps.dsd.io</b></li>
            <li>Production: <b>https://community-api.probation.service.justice.gov.uk</b></li>
            </ul>
            <div>
            """,
        contact = @Contact(
            name = "Probation Integration Team",
            email = "probation-integration-team@digital.justice.gov.uk",
            url = "https://mojdt.slack.com/archives/C02HQ4M2YQN" // #probation-integration-tech Slack channel
        ),
        license = @License(
            name = "MIT",
            url = "https://github.com/ministryofjustice/community-api/blob/main/LICENSE"
        ),
        version = "1.0"
    ),
//    tags = {
//        new Tag().name("-- Popular core APIs --").description("-- Popular core APIs --"),
//        new Tag().name("Convictions").description("Convictions"),
//        new Tag().name("Contact and attendance").description("Contact and attendance"),
//        new Tag().name("Custody").description("Custody"),
//        new Tag().name("Offender managers").description("Offender managers"),
//        new Tag().name("Documents").description("Documents"),
//        new Tag().name("Sentence requirements and breach").description("Sentence requirements and breach"),
//        new Tag().name("Personal circumstances").description("Personal circumstances"),
//        new Tag().name("Risks and Registrations").description("Risks and Registrations"),
//        new Tag().name("Authentication and users").description("Authentication and users"),
//        new Tag().name("Court appearances").description("Court appearances"),
//        new Tag().name("Staff").description("Staff"),
//        new Tag().name("Events").description("Events"),
//        new Tag().name("Reference data").description("Reference data"),
//        new Tag().name("Sentence dates").description("Sentence dates"),
//        new Tag().name("Core offender").description("Core offender")
//    },
    servers = {
        @Server(url = "/")
    },
    security = {
        @SecurityRequirement(name = "hmpps-auth-token")
    }
)
@SecurityScheme(
    name = "hmpps-auth-token",
    scheme = "bearer",
    bearerFormat = "JWT",
    type = SecuritySchemeType.HTTP,
    in = SecuritySchemeIn.HEADER,
    paramName = HttpHeaders.AUTHORIZATION
)
@Configuration
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi communityApi() {
        return GroupedOpenApi.builder()
            .group("Community API")
            .pathsToMatch("/secure/**")
            .addOpenApiCustomizer(openApi -> openApi.tags(List.of(
                new Tag().name("-- Popular core APIs --").description("-- Popular core APIs --"),
                new Tag().name("Convictions").description("Convictions"),
                new Tag().name("Contact and attendance").description("Contact and attendance"),
                new Tag().name("Custody").description("Custody"),
                new Tag().name("Offender managers").description("Offender managers"),
                new Tag().name("Documents").description("Documents"),
                new Tag().name("Sentence requirements and breach").description("Sentence requirements and breach"),
                new Tag().name("Personal circumstances").description("Personal circumstances"),
                new Tag().name("Risks and Registrations").description("Risks and Registrations"),
                new Tag().name("Authentication and users").description("Authentication and users"),
                new Tag().name("Court appearances").description("Court appearances"),
                new Tag().name("Staff").description("Staff"),
                new Tag().name("Events").description("Events"),
                new Tag().name("Reference data").description("Reference data"),
                new Tag().name("Sentence dates").description("Sentence dates"),
                new Tag().name("Core offender").description("Core offender")
            )))
            .build();
    }

    @Bean
    public GroupedOpenApi newTechApi() {
        return GroupedOpenApi.builder()
            .group("NewTech Private APIs")
            .pathsToMatch("/api/**")
            .addOpenApiCustomizer(openApi -> openApi
                .getInfo()
                .description("REST service for Probation API as used by New Tech. These APIs are not be available for clients other than NewTech. Please select Community API from the <b>Select a definition</b> dropdown.")
            )
            .build();
    }
}

//@Configuration
//@EnableSwagger2
//@AllArgsConstructor
//@Import(BeanValidatorPluginsConfiguration.class)
//public class SwaggerConfig {
//    private final BuildProperties buildProperties;
//
//    @Bean
//    public Docket communityAPI() {
//        return new Docket(DocumentationType.SWAGGER_2)
//                .useDefaultResponseMessages(false)
//                .groupName("Community API")
//                .tags(new Tag().name("-- Popular core APIs --").description("-- Popular core APIs --"),
//                        new Tag().name("Convictions").description("Convictions"),
//                        new Tag().name("Contact and attendance").description("Contact and attendance"),
//                        new Tag().name("Custody").description("Custody"),
//                        new Tag().name("Offender managers").description("Offender managers"),
//                        new Tag().name("Documents").description("Documents"),
//                        new Tag().name("Sentence requirements and breach").description("Sentence requirements and breach"),
//                        new Tag().name("Personal circumstances").description("Personal circumstances"),
//                        new Tag().name("Risks and Registrations").description("Risks and Registrations"),
//                        new Tag().name("Authentication and users").description("Authentication and users"),
//                        new Tag().name("Court appearances").description("Court appearances"),
//                        new Tag().name("Staff").description("Staff"),
//                        new Tag().name("Events").description("Events"),
//                        new Tag().name("Reference data").description("Reference data"),
//                        new Tag().name("Sentence dates").description("Sentence dates"),
//                        new Tag().name("Core offender").description("Core offender")
//                )
//                .apiInfo(apiInfo())
//                .select()
//                .apis(RequestHandlerSelectors.any())
//                .paths(
//                        regex("(\\/ping.*)")
//                                .or(regex("(\\/info.*)"))
//                                .or(regex("(\\/health.*)"))
//                                .or(regex("(\\/secure/.*)")))
//                .build()
//                .genericModelSubstitutes(Optional.class)
//                .directModelSubstitute(ZonedDateTime.class, java.util.Date.class)
//                .directModelSubstitute(LocalDateTime.class, java.util.Date.class)
//                .directModelSubstitute(LocalTime.class, String.class)
//                .globalResponses(HttpMethod.GET, getCustomizedResponseMessages())
//                .globalResponses(HttpMethod.PUT, getCustomizedResponseMessages())
//                .globalResponses(HttpMethod.POST, getCustomizedResponseMessages())
//                .globalResponses(HttpMethod.DELETE, getCustomizedResponseMessages())
//                .forCodeGeneration(true);
//    }
//
//    @Bean
//    public Docket newTechAPI() {
//        return new Docket(DocumentationType.SWAGGER_2)
//                .useDefaultResponseMessages(false)
//                .groupName("NewTech Private APIs")
//                .apiInfo(newTechApiInfo())
//                .select()
//                .apis(RequestHandlerSelectors.any())
//                .paths(
//                        regex("(\\/ping.*)")
//                                .or(regex("(\\/info.*)"))
//                                .or(regex("(\\/health.*)"))
//                                .or(regex("(\\/api/.*)")))
//                .build()
//                .genericModelSubstitutes(Optional.class)
//                .directModelSubstitute(ZonedDateTime.class, java.util.Date.class)
//                .directModelSubstitute(LocalDateTime.class, java.util.Date.class)
//                .forCodeGeneration(true);
//    }
//
//    private Contact contactInfo() {
//        return new Contact(
//                "HMPPS Digital Studio",
//                "",
//                "dps-hmpps@digital.justice.gov.uk");
//    }
//
//    private ApiInfo apiInfo() {
//        return new ApiInfo(
//                "Community API Documentation",
//                "<h2>REST service for accessing community probation information</h2>" +
//                        "<p>This service provides endpoints for accessing data primary sourced from National Delius about people that are of interest to HM Probation Service.</p>" +
//                        "<p>There is cross-over with the <b>prison-api</b> though suspects on remand will not be surfaced by this API unless that have previously been on probation.</p>" +
//                        "<div>" +
//                        "This service is secured by <b>OAuth2</b> with tokens supplied by HMPPS Auth. Most read-only endpoints require the <b>ROLE_COMMUNITY</b> to access, but check each endpoint where this differs." +
//                        "<p>This service can be accessed in a number environments. For each environment a different set of OAuth2 credentials from HMPPS Auth are required</p>" +
//                        "<ul>" +
//                        "<li>Development: <b>https://community-api.dev.probation.service.justice.gov.uk</b></li>" +
//                        "<li>Test: <b>https://community-api.test.probation.service.justice.gov.uk</b></li>" +
//                        "<li>Pre-production: <b>https://community-api.pre-prod.delius.probation.hmpps.dsd.io</b></li>" +
//                        "<li>Production: <b>https://community-api.probation.service.justice.gov.uk</b></li>" +
//                        "</ul>" +
//                        "<div>",
//                buildProperties.getVersion(),
//                "https://gateway.nomis-api.service.justice.gov.uk/auth/terms",
//                contactInfo(),
//                "Open Government Licence v3.0", "https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/", List
//                .of());
//    }
//
//    private ApiInfo newTechApiInfo() {
//        return new ApiInfo(
//                "New Tech Private API Documentation",
//                "REST service for Probation API as used by New Tech. These APIs are not be available for clients other than NewTech. Please select Community API from the <b>Select a definition</b> dropdown.",
//                buildProperties.getVersion(),
//                "https://gateway.nomis-api.service.justice.gov.uk/auth/terms",
//                contactInfo(),
//                "Open Government Licence v3.0", "https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/", List
//                .of());
//    }
//
//    private List<Response> getCustomizedResponseMessages() {
//        List<Response> responseMessages = new ArrayList<>();
//        responseMessages.add(new Response("401", "JWT supplied invalid or absent", true, List.of(), List.of(), List.of(), List
//                .of()));
//        responseMessages.add(new Response("403", "Requires role ROLE_COMMUNITY", true, List
//                .of(), List.of(), List.of(), List.of()));
//        return responseMessages;
//    }
//
//    @Bean
//    public JacksonModuleRegistrar swaggerJacksonModuleRegistrar() {
//        return ReferenceSerializationConfigurer::serializeAsComputedRef;
//    }
//
//    /**
//     * HACK: fix for springfox on spring 2.6
//     * https://github.com/springfox/springfox/issues/3462#issuecomment-1010721223
//     */
//    @Bean
//    @ConditionalOnWebApplication
//    public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(
//            WebEndpointsSupplier webEndpointsSupplier,
//            ServletEndpointsSupplier servletEndpointsSupplier,
//            ControllerEndpointsSupplier controllerEndpointsSupplier,
//            EndpointMediaTypes endpointMediaTypes,
//            CorsEndpointProperties corsProperties,
//            WebEndpointProperties webEndpointProperties,
//            Environment environment) {
//        List<ExposableEndpoint<?>> allEndpoints = new ArrayList<>();
//        Collection<ExposableWebEndpoint> webEndpoints = webEndpointsSupplier.getEndpoints();
//        allEndpoints.addAll(webEndpoints);
//        allEndpoints.addAll(servletEndpointsSupplier.getEndpoints());
//        allEndpoints.addAll(controllerEndpointsSupplier.getEndpoints());
//        String basePath = webEndpointProperties.getBasePath();
//        EndpointMapping endpointMapping = new EndpointMapping(basePath);
//        boolean shouldRegisterLinksMapping = this.shouldRegisterLinksMapping(webEndpointProperties, environment, basePath);
//        return new WebMvcEndpointHandlerMapping(endpointMapping, webEndpoints, endpointMediaTypes,
//            corsProperties.toCorsConfiguration(), new EndpointLinksResolver(allEndpoints, basePath),
//            shouldRegisterLinksMapping);
//    }
//
//    private boolean shouldRegisterLinksMapping(WebEndpointProperties webEndpointProperties, Environment environment, String basePath) {
//        return webEndpointProperties.getDiscovery().isEnabled()
//            && (StringUtils.hasText(basePath) || ManagementPortType.get(environment).equals(ManagementPortType.DIFFERENT));
//    }
//}
