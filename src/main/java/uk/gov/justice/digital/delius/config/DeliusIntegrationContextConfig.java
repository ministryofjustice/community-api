package uk.gov.justice.digital.delius.config;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
@ConfigurationProperties(prefix = "delius-integration-context")
@Data
@Slf4j
public class DeliusIntegrationContextConfig {

    @Data
    public static class IntegrationContext {

        private String providerCode;
        private String staffCode;
        private String teamCode;
        private String requirementRehabilitationActivityType;
        private NsiMapping nsiMapping = new NsiMapping();
        private ContactMapping contactMapping = new ContactMapping();
    }


    @Data
    public static class NsiMapping {
        private String nsiStatus;
        private Map<UUID, String> serviceCategoryToNsiType;
    }

    @Data
    public static class ContactMapping {
        private String appointmentContactType;
    }

    private Map<String, IntegrationContext> integrationContexts = new HashMap<>();
}
