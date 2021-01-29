package uk.gov.justice.digital.delius.config;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Log4j2
@AllArgsConstructor
@ConditionalOnExpression("T(org.apache.commons.lang3.StringUtils).isNotBlank('${applicationinsights.connection.string:}')")
public class ClientTrackingConfiguration implements WebMvcConfigurer {
    private final ClientTrackingInterceptor clientTrackingInterceptor;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        log.info("Adding application insights client tracking interceptor");
        registry.addInterceptor(clientTrackingInterceptor).addPathPatterns("/**");
    }
}
