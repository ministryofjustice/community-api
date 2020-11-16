package uk.gov.justice.digital.delius.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class SecurityUserContext {

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public boolean isClientOnly() {
        final var authentication = getAuthentication();
        if (!(authentication instanceof AuthAwareAuthenticationToken)) return false;

        final var token = (AuthAwareAuthenticationToken) authentication;
        return token.isClientOnly();
    }

    public String getDatabaseUsername() {
        final var authentication = getAuthentication();
        if (!(authentication instanceof AuthAwareAuthenticationToken)) return null;

        final var token = (AuthAwareAuthenticationToken) authentication;
        return token.getDatabaseUsername();
    }

    public boolean isSecure() {
       return getAuthentication() != null;
    }

    public Optional<String> getCurrentUsername() {
        final var authentication = getAuthentication();
        if (!(authentication instanceof AuthAwareAuthenticationToken)) return Optional.empty();

        final var token = (AuthAwareAuthenticationToken) authentication;
        if (token.isClientOnly()) return Optional.empty();

        return Optional.of(token.getSubject());
    }

}
