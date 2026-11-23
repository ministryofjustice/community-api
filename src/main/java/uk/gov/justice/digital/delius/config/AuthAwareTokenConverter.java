package uk.gov.justice.digital.delius.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import static uk.gov.justice.hmpps.kotlin.auth.AuthAwareTokenConverterKt.extractAuthorities;

public class AuthAwareTokenConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(final Jwt jwt) {
        final var clientId = jwt.getClaims().get("client_id");
        final var clientOnly = jwt.getSubject().equals(clientId);
        return new AuthAwareAuthenticationToken(jwt, clientOnly, extractAuthorities(jwt, jwtGrantedAuthoritiesConverter));
    }
}
