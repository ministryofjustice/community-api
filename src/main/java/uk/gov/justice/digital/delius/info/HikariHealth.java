package uk.gov.justice.digital.delius.info;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.SortedSet;

@Component
@Slf4j
public class HikariHealth implements HealthIndicator {

    @Autowired
    private HealthCheckRegistry healthCheckRegistry;

    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();
        builder.down();

        SortedSet<String> healthCheckRegistryNames = healthCheckRegistry.getNames();

        boolean up = true;
        for (String name : healthCheckRegistryNames) {
            HealthCheck.Result result = healthCheckRegistry.getHealthCheck(name).execute();
            builder.withDetail(name, result);
            up &= result.isHealthy();
        }

        if (up) {
            builder.up();
        }

        return builder.build();
    }
}