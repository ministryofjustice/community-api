package uk.gov.justice.digital.delius.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.user.UserData;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

@Component
public class Jwt {

    public static final String UID = "uid";
    public static final String PROBATION_AREA_CODES = "probationAreaCodes";

    private final SecretKey key;
    private final int lifetimeSeconds;
    private final JwtParser parser;

    public Jwt(@Value("${jwt.secret}") String secret,
               @Value("${jwt.lifetimeSeconds:300}") int lifetimeSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.lifetimeSeconds = lifetimeSeconds;
        this.parser = Jwts.parser()
            .verifyWith(key)
            .build();
    }

    public Optional<Claims> parseToken(String bearerToken) {
        return Optional.ofNullable(parser.parseSignedClaims(bearerToken).getPayload());
    }

    public Optional<Claims> parseAuthorizationHeader(String authorizationHeader) {
        return parseToken(authorizationHeader.replace("Bearer ", "").replace("bearer ", ""));
    }

    public String buildToken(UserData userData) {

        Claims claims = Jwts.claims()
            .subject(userData.getDistinguishedName())
            .add(UID, userData.getUid())
            .add(PROBATION_AREA_CODES, userData.getProbationAreaCodes())
            .build();

        return Jwts.builder()
                .claims(claims)
                .expiration(Date.from(java.time.ZonedDateTime.now().plusSeconds(lifetimeSeconds).toInstant()))
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }
}
