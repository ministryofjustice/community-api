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
        if (getAuthentication() instanceof AuthAwareAuthenticationToken) {
            return getOptionalCurrentUser().isEmpty(); // TODO DT-838 This needs explicitly testing
        }
        return false;
    }

    public boolean isSecure() {
       return getAuthentication() != null;
    }

    public Optional<String> getCurrentUsername() {
        return getOptionalCurrentUser().map(UserIdUser::getUsername);
    }

    public Optional<UserIdUser> getOptionalCurrentUser() {
        final Authentication authentication = getAuthentication();
        if (!(authentication instanceof AuthAwareAuthenticationToken)) return Optional.empty();

        return Optional.of(((AuthAwareAuthenticationToken) authentication).getUserIdUser());
    }

}
