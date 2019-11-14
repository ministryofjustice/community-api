package uk.gov.justice.digital.delius.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class SecurityUserContext {

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public boolean isClientOnly() {
        if (getAuthentication() instanceof OAuth2Authentication) {
            return ((OAuth2Authentication)getAuthentication()).isClientOnly();
        }
        return false;
    }

    public boolean isSecure() {
       return getAuthentication() != null;
    }

    public Optional<String> getCurrentUsername() {
        final Authentication authentication = getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) return Optional.empty();

        final Object userPrincipal = authentication.getPrincipal();

        final String username;
        if (userPrincipal instanceof String) {
            username = (String) userPrincipal;
        } else if (userPrincipal instanceof UserDetails) {
            username = ((UserDetails) userPrincipal).getUsername();
        } else if (userPrincipal instanceof Map) {
            final Map<?, ?> userPrincipalMap = (Map<?, ?>) userPrincipal;
            username = (String) userPrincipalMap.get("username");
        } else {
            username = userPrincipal.toString();
        }

        if (StringUtils.isEmpty(username) || username.equals("anonymousUser")) return Optional.empty();

        log.debug("Authentication doesn't contain user id, using username instead");
        return Optional.of(username);
    }

}
