package uk.gov.justice.digital.delius.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import org.junit.Test;
import uk.gov.justice.digital.delius.user.UserData;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class JwtTest {

    @Test
    public void canGenerateJwt() {
        Jwt jwt = new Jwt("a secret", 1);

        String token = jwt.buildToken(UserData.builder().distinguishedName("Colin").build());

        //Looks like a token
        assertThat(token.split("\\.").length).isEqualTo(3);
    }

    @Test
    public void canDecodeOwnSignedJwt() {
        Jwt jwt = new Jwt("a secret", 1);

        String distinguishedName = UUID.randomUUID().toString();
        String token = jwt.buildToken(UserData.builder().distinguishedName(distinguishedName).build());

        Optional<Claims> claims = jwt.parseToken(token);

        assertThat(claims.isPresent()).isTrue();
        assertThat(claims.get().getSubject()).isEqualTo(distinguishedName);

    }

    @Test(expected = SignatureException.class)
    public void cannotDecodeSomebodyElsesSignedJwt() {
        Jwt jwt = new Jwt("a secret", 1);

        String token = jwt.buildToken(UserData.builder().distinguishedName("Colin").build());

        Jwt jwtOther = new Jwt("a different secret", 1);
        Optional<Claims> claims = jwtOther.parseToken(token);

        fail("Should have failed signature validation.");
    }

    @Test(expected = MalformedJwtException.class)
    public void cannotDecodeRubbishJwt() {
        Jwt jwt = new Jwt("a secret", 1);

        jwt.parseToken("utter rubbish..");

        fail("Should have failed to parse malformed token");
    }

    @Test(expected = ExpiredJwtException.class)
    public void cannotDecodeExpiredJwt() throws InterruptedException {
        Jwt jwt = new Jwt("a secret", 1);

        String token = jwt.buildToken(UserData.builder().distinguishedName("Colin").build());

        Thread.sleep(1000);

        jwt.parseToken(token);

        fail("Should have failed to parse expired token");
    }

    @Test
    public void canDecodeAuthorizationHeaderWithOwnSignedJwt() {
        Jwt jwt = new Jwt("a secret", 1);

        String distinguishedName = UUID.randomUUID().toString();
        String token = jwt.buildToken(UserData.builder().distinguishedName(distinguishedName).build());

        Optional<Claims> claims = jwt.parseAuthorizationHeader("Bearer " + token);

        assertThat(claims.isPresent()).isTrue();
        assertThat(claims.get().getSubject()).isEqualTo(distinguishedName);

        claims = jwt.parseAuthorizationHeader("bearer " + token);

        assertThat(claims.isPresent()).isTrue();
        assertThat(claims.get().getSubject()).isEqualTo(distinguishedName);

    }
}