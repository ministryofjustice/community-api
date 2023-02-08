package uk.gov.justice.digital.delius.config;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.opentelemetry.api.trace.Span;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.Optional;

@Slf4j
@Configuration
public class ClientTrackingInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(final HttpServletRequest request, @NotNull final HttpServletResponse response, @NotNull final Object handler) {
        final var token = request.getHeader(HttpHeaders.AUTHORIZATION);
        final var bearer = "Bearer ";
        if (StringUtils.startsWithIgnoreCase(token, bearer)) {
            try {
                final var jwtBody = getClaimsFromJWT(token);
                final var user = Optional.ofNullable(jwtBody.getClaim("user_name"));
                user.map(String::valueOf).ifPresent(u -> Span.current().setAttribute("username", u));
                Span.current().setAttribute("clientId", String.valueOf(jwtBody.getClaim("client_id")));

            } catch (final ParseException e) {
                // we have a bearer token we don't understand.
                // this can happen from AWS health checks for instance - so just silently ignore
            }
        }
        return true;
    }

    private JWTClaimsSet getClaimsFromJWT(final String token) throws ParseException {
        final var signedJWT = SignedJWT.parse(token.replace("Bearer ", ""));
        return signedJWT.getJWTClaimsSet();
    }
}

