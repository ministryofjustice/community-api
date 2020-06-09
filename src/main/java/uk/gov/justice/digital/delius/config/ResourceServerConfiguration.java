package uk.gov.justice.digital.delius.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
public class ResourceServerConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(final HttpSecurity http) throws Exception {
        http
                .antMatcher("/secure/**")
                .authorizeRequests( auth ->
                        auth.antMatchers("/secure/**").authenticated()
                )
                .oauth2ResourceServer().jwt().jwtAuthenticationConverter(new AuthAwareTokenConverter());
    }
}
