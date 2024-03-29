package uk.gov.justice.digital.delius.jwt;

import io.jsonwebtoken.Claims;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import uk.gov.justice.digital.delius.config.SecurityUserContext;
import uk.gov.justice.digital.delius.exception.JwtTokenMissingException;
import uk.gov.justice.digital.delius.helpers.CurrentUserSupplier;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JwtValidatorTest {
    private Jwt jwt = mock(Jwt.class);
    private JoinPoint joinPoint = mock(JoinPoint.class);
    private HttpHeaders httpHeaders = mock(HttpHeaders.class);
    private Claims claims = mock(Claims.class);
    private Signature signature = mock(Signature.class);
    private SecurityUserContext securityUserContext = mock(SecurityUserContext.class);

    private JwtValidator jwtValidator = new JwtValidator(jwt);
    private CurrentUserSupplier currentUserSupplier = new CurrentUserSupplier(securityUserContext);

    @BeforeEach
    public void setup() {
        given(joinPoint.getArgs()).willReturn(new Object[]{httpHeaders});
        when(signature.getName()).thenReturn("myMethod");
        given(joinPoint.getSignature()).willReturn(signature);
        given(jwt.parseAuthorizationHeader("some.jwt.token")).willReturn(Optional.of(claims));
        when(securityUserContext.isSecure()).thenReturn(false);
        when(claims.get(Jwt.UID)).thenReturn("john.smith");
    }

    @Test
    public void parsesJwtTokenSuccessfullyWhenAuthorizationHeaderIsPresent() {
        given(joinPoint.getArgs()).willReturn(new Object[]{httpHeaders});
        given(httpHeaders.getFirst("Authorization")).willReturn("some.jwt.token");
        jwtValidator.validateJwt(joinPoint);
        assertThat(currentUserSupplier.username()).isPresent();
    }

    @Test
    public void throwsExceptionIfAuthorizationTokenIsMissing() {
        given(httpHeaders.getFirst("Authorization")).willReturn(null);
        assertThrows(JwtTokenMissingException.class,
                     () -> { jwtValidator.validateJwt(joinPoint); });
    }
}
