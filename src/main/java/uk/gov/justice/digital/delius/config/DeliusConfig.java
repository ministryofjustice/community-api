package uk.gov.justice.digital.delius.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class DeliusConfig {

    @Bean("deliusRestTemplateWithAuth")
    public RestTemplate deliusRestTemplateWithAuth(final RestTemplateBuilder restTemplateBuilder,
                                                   @Value("${delius.baseurl}") final String baseUrl,
                                                   @Value("${delius.username}") final String deliusUsername,
                                                   @Value("${delius.password}") final String deliusPassword,
                                                   @Value("${delius.connectTimeout:30s}") final Duration connectTimeout,
                                                   @Value("${delius.readTimeout:30s}") final Duration readTimeout) {
        return restTemplateBuilder
                .rootUri(baseUrl)
                .basicAuthentication(deliusUsername, deliusPassword)
                .setConnectTimeout(connectTimeout)
                .setReadTimeout(readTimeout)
                .build();
    }
}
