package uk.gov.justice.digital.delius.jwt;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.config.ApplicationInsightsConfiguration;
import uk.gov.justice.digital.delius.exception.JwtTokenMissingException;
import uk.gov.justice.digital.delius.helpers.CurrentUserSupplier;

import java.util.*;

@Aspect
@Component
@Slf4j
public class JwtValidator {

    private final Jwt jwt;

    public JwtValidator(@Autowired Jwt jwt) {
        this.jwt = jwt;
    }

    @Before("execution(@uk.gov.justice.digital.delius.jwt.JwtValidation * *(..))")
    public void validateJwt(JoinPoint joinPoint) {
        ArrayList<Object> argsList = new ArrayList<>(List.of(joinPoint.getArgs()));

        Optional<Claims> maybeClaims = argsList.stream()
                .filter(HttpHeaders.class::isInstance)
                .findFirst()
                .map(headers -> ((HttpHeaders) headers).getFirst("Authorization"))
                .map(jwt::parseAuthorizationHeader)
                .orElseThrow(() -> new JwtTokenMissingException("No Authorization Bearer token found in headers."));

        maybeClaims.ifPresent(ApplicationInsightsConfiguration::setNewTechClientId);
        maybeClaims.ifPresent(CurrentUserSupplier::setClaims);
    }

}
