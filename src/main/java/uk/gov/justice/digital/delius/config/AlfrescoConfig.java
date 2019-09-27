package uk.gov.justice.digital.delius.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import uk.gov.justice.digital.delius.utils.W3cTracingInterceptor;

import java.util.Collections;
import java.util.List;

@Configuration
public class AlfrescoConfig {

    @Bean
    public RestTemplate alfrescoRestTemplate(RestTemplateBuilder restTemplateBuilder,
                                             @Value("${alfresco.baseUrl}") String baseUrl) {
        return restTemplateBuilder
                .rootUri(baseUrl)
                .additionalInterceptors(getRequestInterceptors())
                .build();
    }

    private List<ClientHttpRequestInterceptor> getRequestInterceptors() {
        return Collections.singletonList(new W3cTracingInterceptor());
    }
}
