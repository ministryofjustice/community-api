package uk.gov.justice.digital.delius.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AlfrescoConfig {

    @Bean("alfrescoWebClient")
    public WebClient alfrescoWebClient(
            WebClient.Builder builder,
            @Value("${alfresco.baseUrl}") String baseUrl
    ) {
        return builder.baseUrl(baseUrl).build();
    }
}
