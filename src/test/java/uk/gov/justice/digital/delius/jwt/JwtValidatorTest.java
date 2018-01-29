package uk.gov.justice.digital.delius.jwt;

import io.jsonwebtoken.Claims;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import uk.gov.justice.digital.delius.exception.JwtTokenMissingException;
import uk.gov.justice.digital.delius.jpa.oracle.UserProxy;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JwtValidatorTest {
    private Jwt jwt = mock(Jwt.class);
    private JoinPoint joinPoint = mock(JoinPoint.class);
    private HttpHeaders httpHeaders = mock(HttpHeaders.class);
    private Claims claims = mock(Claims.class);
    private Signature signature = mock(Signature.class);

    private JwtValidator jwtValidator = new JwtValidator(jwt);

    @Before
    public void setup() {
        given(joinPoint.getArgs()).willReturn(new Object[]{httpHeaders});
        when(signature.getName()).thenReturn("myMethod");
        given(joinPoint.getSignature()).willReturn(signature);
        given(jwt.parseAuthorizationHeader("some.jwt.token")).willReturn(Optional.of(claims));
    }

    @Test
    public void parsesJwtTokenSuccessfullyWhenAuthorizationHeaderIsPresent() {
        given(httpHeaders.getFirst("Authorization")).willReturn("some.jwt.token");

        jwtValidator.validateJwt(joinPoint);

        assertThat(UserProxy.threadLocalClaims.get()).isNotNull();
    }

    @Test(expected = JwtTokenMissingException.class)
    public void throwsExceptionIfAuthorizationTokenIsMissing() {
        given(httpHeaders.getFirst("Authorization")).willReturn(null);

        jwtValidator.validateJwt(joinPoint);
    }
}
