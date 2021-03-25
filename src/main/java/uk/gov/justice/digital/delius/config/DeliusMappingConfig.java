package uk.gov.justice.digital.delius.config;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "interventions-delius")
@Data
@Slf4j
public class DeliusMappingConfig {

    @Data
    public static class NsiMapping {
        private String providerCode;
        private String staffCode;
        private String teamCode;
        private String nsiStatus;
        private Map<String, String> serviceCategoryToNsiType;
    }

    private NsiMapping nsiMapping = new NsiMapping();
}
