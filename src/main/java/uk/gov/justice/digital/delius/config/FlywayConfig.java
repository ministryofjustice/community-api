package uk.gov.justice.digital.delius.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ConditionalOnProperty("spring.datasource.url")
@PropertySource("classpath:noflyway.properties")
public class FlywayConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer flywayConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
