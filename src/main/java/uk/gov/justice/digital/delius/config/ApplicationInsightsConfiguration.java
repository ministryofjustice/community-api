package uk.gov.justice.digital.delius.config;

import com.microsoft.applicationinsights.TelemetryClient;
import io.opentelemetry.api.trace.Span;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application insights now controlled by the spring-boot-starter dependency.  However when the key is not specified
 * we don't get a telemetry bean and application won't start.  Therefore need this backup configuration.
 */
@Configuration
@Log4j2
public class ApplicationInsightsConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public TelemetryClient telemetryClient() {
        log.warn("Application insights configuration missing, returning dummy bean instead");
        return new TelemetryClient();
    }

    public static void setNewTechClientId(Object any) {
        Span.current().setAttribute("clientId", "delius-new-tech");
    }
}
