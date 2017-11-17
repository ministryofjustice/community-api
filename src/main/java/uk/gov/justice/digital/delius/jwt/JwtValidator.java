package uk.gov.justice.digital.delius.jwt;

import io.jsonwebtoken.Claims;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.exception.JwtTokenMissingException;
import uk.gov.justice.digital.delius.jpa.oracle.UserProxy;

import java.util.Arrays;
import java.util.Optional;

@Aspect
@Component
public class JwtValidator {

    private Jwt jwt;

    public JwtValidator(@Autowired Jwt jwt) {
        this.jwt = jwt;
    }

    @Before("execution(@uk.gov.justice.digital.delius.jwt.JwtValidation * *(..))")
    public void validateJwt(JoinPoint joinPoint) {

        Object[] args = joinPoint.getArgs();

        Optional<Claims> maybeClaims = Arrays.stream(args)
                .filter(arg -> arg instanceof HttpHeaders)
                .findFirst()
                .map(headers -> ((HttpHeaders) headers).getFirst("Authorization"))
                .map(authorization -> jwt.parseAuthorizationHeader(authorization))
                .orElseThrow(() -> new JwtTokenMissingException("No Authorization Bearer token found in headers."));

        if (maybeClaims.isPresent()) {
            UserProxy.threadLocalClaims.set(maybeClaims.get());
        }
    }

}
