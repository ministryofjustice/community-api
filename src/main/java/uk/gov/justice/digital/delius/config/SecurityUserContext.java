package uk.gov.justice.digital.delius.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.config.UserIdAuthenticationConverter.UserIdUser;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class SecurityUserContext {

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public Optional<String> getCurrentUsername() {
        return getOptionalCurrentUser().map(User::getUsername);
    }

    private Optional<UserIdUser> getOptionalCurrentUser() {
        final Authentication authentication = getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) return Optional.empty();

        final Object userPrincipal = authentication.getPrincipal();

        if (userPrincipal instanceof UserIdUser) return Optional.of((UserIdUser) userPrincipal);

        final String username;
        if (userPrincipal instanceof String) {
            username = (String) userPrincipal;
        } else if (userPrincipal instanceof UserDetails) {
            username = ((UserDetails) userPrincipal).getUsername();
        } else if (userPrincipal instanceof Map) {
            final Map userPrincipalMap = (Map) userPrincipal;
            username = (String) userPrincipalMap.get("username");
        } else {
            username = userPrincipal.toString();
        }

        if (StringUtils.isEmpty(username) || username.equals("anonymousUser")) return Optional.empty();

        log.debug("Authentication doesn't contain user id, using username instead");
        return Optional.of(new UserIdUser(username, authentication.getCredentials().toString(), authentication.getAuthorities(), username));
    }

}
