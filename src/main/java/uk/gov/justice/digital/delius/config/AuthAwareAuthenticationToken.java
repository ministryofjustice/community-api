package uk.gov.justice.digital.delius.config;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;

@Getter
public class AuthAwareAuthenticationToken extends JwtAuthenticationToken {

    private final String subject;
    private final boolean clientOnly;

    public AuthAwareAuthenticationToken(Jwt jwt, boolean clientOnly, Collection<? extends GrantedAuthority> authorities) {
        super(jwt, authorities);
        subject = jwt.getSubject();
        this.clientOnly = clientOnly;
    }
}
