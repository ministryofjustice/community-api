package uk.gov.justice.digital.delius.config;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import io.opentelemetry.api.trace.Span;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Log4j2
@AllArgsConstructor
public class ClientTrackingConfiguration implements WebMvcConfigurer {
    private final ClientTrackingInterceptor clientTrackingInterceptor;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(clientTrackingInterceptor).addPathPatterns("/**");
    }
}
