package uk.gov.justice.digital.delius.config;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        private Map<String, String> contractTypeToNsiType;
    }

    @Data
    public static class ContactMapping {
        private String notificationContactType;
        private String appointmentRarContactType;
        private String appointmentNonRarContactType;
        private String enforcementReferToOffenderManager;
        private Map<String, String> endTypeToOutcomeType;
        private Map<String, Map<Boolean, String>> attendanceAndBehaviourNotifiedMappingToOutcomeType;

        public List<String> getAllAppointmentContactTypes() {
            return List.of(appointmentRarContactType, appointmentNonRarContactType);
        }
    }

    private Map<String, IntegrationContext> integrationContexts = new HashMap<>();
}
