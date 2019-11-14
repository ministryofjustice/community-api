package uk.gov.justice.digital.delius.info;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

/**
 * Adds version data to the /health endpoint. This is called by the UI to display API details
 */
@Component
public class HealthInfo implements HealthIndicator {

    @Autowired(required = false)
    private BuildProperties buildProperties;

    @Override
    public Health health() {
        return Health.up().withDetail("version", getVersion()).build();
    }

    private String getVersion() {
        return buildProperties == null ? "version not available" : buildProperties.getVersion();
    }

}
