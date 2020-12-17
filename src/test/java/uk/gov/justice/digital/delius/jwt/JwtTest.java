package uk.gov.justice.digital.delius.jwt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.user.UserData;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JwtTest {

    @Test
    public void canGenerateJwt() {
        Jwt jwt = new Jwt("a secret", 1);

        String token = jwt.buildToken(UserData.builder().distinguishedName("Colin").build());

        // Looks like a token
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

    @Test
    public void cannotDecodeSomebodyElsesSignedJwt() {
        Jwt jwt = new Jwt("a secret", 1);

        String token = jwt.buildToken(UserData.builder().distinguishedName("Colin").build());

        Jwt jwtOther = new Jwt("a different secret", 1);

        assertThrows(SignatureException.class,
            () -> { jwtOther.parseToken(token); }, "Should have failed signature validation.");
    }

    @Test
    public void cannotDecodeRubbishJwt() {
        Jwt jwt = new Jwt("a secret", 1);

        assertThrows(MalformedJwtException.class,
            () -> { jwt.parseToken("utter rubbish.."); },
            "Should have failed to parse malformed token");
    }

    @Test
        public void cannotDecodeExpiredJwt() throws InterruptedException {
        Jwt jwt = new Jwt("a secret", 1);

        String token = jwt.buildToken(UserData.builder().distinguishedName("Colin").build());

        Thread.sleep(1000);

        assertThrows(ExpiredJwtException.class,
            () -> { jwt.parseToken(token); },"Should have failed to parse expired token");
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

    @Test
    public void cannotModifyClaimsAndStillHaveValidToken() throws IOException {
        Jwt jwt = new Jwt("a secret", 1);

        String distinguishedName = UUID.randomUUID().toString();
        String uid = UUID.randomUUID().toString();

        String originalToken = jwt.buildToken(UserData.builder()
                .distinguishedName(distinguishedName)
                .uid(uid)
                .build());

        String[] tokenParts = originalToken.split("\\.");

        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, String> payloadMap = objectMapper.readValue(Base64.getDecoder().decode(tokenParts[1]), new TypeReference<Map<String, String>>() {});

        payloadMap.put("extra", "naughty_claim");

        String serializedClaims = objectMapper.writeValueAsString(payloadMap);

        String modifiedPart2 = new String(Base64.getEncoder().encode(serializedClaims.getBytes()));

        tokenParts[1] = modifiedPart2;

        String tinkeredToken = String.join(".", tokenParts);

        assertThrows(SignatureException.class,
            () -> { jwt.parseToken(tinkeredToken); },
            "Should have rejected modified content with invalid signature.");
    }
}
