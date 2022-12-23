package uk.gov.justice.digital.delius.jwt;

import com.google.common.collect.Lists;
import com.microsoft.applicationinsights.telemetry.RequestTelemetry;
import com.microsoft.applicationinsights.web.internal.RequestTelemetryContext;
import com.microsoft.applicationinsights.web.internal.ThreadContext;
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

import java.util.ArrayList;
import java.util.Optional;

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
        ArrayList<Object> argsList = Lists.newArrayList(joinPoint.getArgs());

        Optional<Claims> maybeClaims = argsList.stream()
                .filter(arg -> arg instanceof HttpHeaders)
                .findFirst()
                .map(headers -> ((HttpHeaders) headers).getFirst("Authorization"))
                .map(jwt::parseAuthorizationHeader)
                .orElseThrow(() -> new JwtTokenMissingException("No Authorization Bearer token found in headers."));

        maybeClaims.ifPresent(ApplicationInsightsConfiguration::setNewTechClientId);
        maybeClaims.ifPresent(CurrentUserSupplier::setClaims);
    }

}
