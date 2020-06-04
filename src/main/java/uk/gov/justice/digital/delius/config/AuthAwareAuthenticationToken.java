package uk.gov.justice.digital.delius.config;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;

@Getter
public class AuthAwareAuthenticationToken extends JwtAuthenticationToken {

    private final UserIdUser userIdUser;

    public AuthAwareAuthenticationToken(Jwt jwt, UserIdUser user, Collection<? extends GrantedAuthority> authorities) {
        super(jwt, authorities);
        userIdUser = user;
    }
}
