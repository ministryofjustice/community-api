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
        final var optionalUserId = Optional.ofNullable(jwt.getClaims().get("user_id"));
        final var userIdOrName = optionalUserId.orElseGet(jwt::getSubject);
        return new AuthAwareAuthenticationToken(jwt, (String)userIdOrName, extractAuthorities(jwt));
    }

    private Collection<GrantedAuthority> extractAuthorities(final Jwt jwt) {
        final Collection<String> authorities = (Collection<String>)jwt.getClaims().getOrDefault("authorities", List.of());
        return authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toUnmodifiableSet());
    }
}
