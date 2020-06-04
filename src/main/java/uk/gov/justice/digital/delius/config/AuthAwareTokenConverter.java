package uk.gov.justice.digital.delius.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AuthAwareTokenConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(final Jwt jwt) {
        final var claims = jwt.getClaims();
        var userId = getOrBlank((String)claims.get("user_id"));
        if (userId.isEmpty()) {
            userId = jwt.getSubject();
        }
        var userName = getOrBlank((String)claims.get("user_name"));
        if (userName.isEmpty()) {
            userName = getOrBlank((String)claims.get("client_id"));
        }
        if (userName.isEmpty()) {
            userName = userId;
        }
        return new AuthAwareAuthenticationToken(jwt, new UserIdUser(userName, userId), extractAuthorities(jwt));
    }

    private String getOrBlank(String maybeString) {
        return Optional.ofNullable(maybeString).map(Object::toString).orElseGet(() -> "");
    }

    private Collection<GrantedAuthority> extractAuthorities(final Jwt jwt) {
        final Collection<String> authorities = (Collection<String>)jwt.getClaims().getOrDefault("authorities", List.of());
        return authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toUnmodifiableSet());
    }
}
