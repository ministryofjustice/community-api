package uk.gov.justice.digital.delius.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
@Slf4j
public class ApplicationConfig {

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        customiseObjectMapper(objectMapper);
        jsonConverter.setObjectMapper(objectMapper);
        return jsonConverter;
    }

    public static ObjectMapper customiseObjectMapper(final ObjectMapper objectMapper) {
        return objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .registerModules(new Jdk8Module(), new JavaTimeModule());
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> buildInfoLogger() {
        return event -> {
            try {
                log.debug("BUILD PROPERTIES:");
                BuildProperties buildProperties = (BuildProperties) event.getApplicationContext().getBean("buildProperties");
                buildProperties.iterator().forEachRemaining(prop -> log.debug("{} : {}", prop.getKey(), prop.getValue()));
            } catch (NoSuchBeanDefinitionException nsbde) {
                log.warn("No build info found! Is this a local build?");
            }
        };
    }
}
