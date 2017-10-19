package uk.gov.justice.digital.delius.jwt;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.exception.JwtTokenMissingException;

import java.util.Arrays;

@Aspect
@Component
public class JwtValidator {

    @Autowired
    private Jwt jwt;

    @Before("execution(@uk.gov.justice.digital.delius.jwt.JwtValidation * *(..))")
    public void validateJwt(JoinPoint joinPoint) {

        Object[] args = joinPoint.getArgs();

        Arrays.stream(args)
                .filter(arg -> arg instanceof HttpHeaders)
                .findFirst()
                .map(headers -> ((HttpHeaders) headers).getFirst("Authorization"))
                .map(authorization -> jwt.parseAuthorizationHeader(authorization))
                .orElseThrow(() -> new JwtTokenMissingException("No Authorization Bearer token found in headers."));

    }

}
