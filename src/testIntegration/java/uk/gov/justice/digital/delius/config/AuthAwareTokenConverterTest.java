package uk.gov.justice.digital.delius.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.gov.justice.digital.delius.JwtAuthenticationHelper;
import uk.gov.justice.digital.delius.JwtParameters;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {JwtAuthenticationHelper.class})
public class AuthAwareTokenConverterTest {

    @Autowired
    private JwtAuthenticationHelper jwtAuthenticationHelper;

    private AuthAwareTokenConverter authenticationConverter = new AuthAwareTokenConverter();

    @Test
    public void thatScopesAreAddedToAuthorities() {
        final var token = jwtAuthenticationHelper.createJwt(JwtParameters.builder().build());
        final var jwt = Jwt.withTokenValue(token)
            .subject("some_subject")
            .claim("client_id", "some_client")
            .claim("authorities", List.of("ROLE_some_role"))
            .claim("scope", List.of("some_scope"))
            .expiresAt(Instant.now().plusSeconds(3600))
            .header("typ", "JWT")
            .build();

        final var authToken = authenticationConverter.convert(jwt);

        assertThat(authToken.getAuthorities().toArray(new GrantedAuthority[]{})).extracting(GrantedAuthority::getAuthority).containsExactlyInAnyOrder("ROLE_some_role", "SCOPE_some_scope");
    }
}
