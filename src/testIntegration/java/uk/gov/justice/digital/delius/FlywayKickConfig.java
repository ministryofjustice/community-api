package uk.gov.justice.digital.delius;

import org.flywaydb.core.Flyway;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@TestConfiguration
public class FlywayKickConfig {
    @Bean
    ApplicationRunner runFlyway(DataSource dataSource) {
        return args -> Flyway.configure()
            .dataSource(dataSource)
            // Use the *exact* folders that contain your files
            .locations("classpath:db/schema", "classpath:db/data")
            .failOnMissingLocations(true)
            .load()
            .migrate();
    }
}
