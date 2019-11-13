package uk.gov.justice.digital.delius.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AlfrescoConfig {

    @Bean("alfrescoRestTemplate")
    public RestTemplate alfrescoRestTemplate(RestTemplateBuilder restTemplateBuilder,
                                             @Value("${alfresco.baseUrl}") String baseUrl) {
        return restTemplateBuilder
                .rootUri(baseUrl)
                .build();
    }

    @Bean("alfrescoRestTemplateWithAuth")
    public RestTemplate alfrescoRestTemplateWithAuth(RestTemplateBuilder restTemplateBuilder,
                                                     @Value("${delius.baseurl}") String baseUrl,
                                                     @Value("${delius.username}") String deliusUsername,
                                                     @Value("${delius.password}") String deliusPassword) {
        return restTemplateBuilder
                .rootUri(baseUrl)
                .basicAuthentication(deliusUsername, deliusPassword)
                .build();
    }
}
