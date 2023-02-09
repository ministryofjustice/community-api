package uk.gov.justice.digital.delius.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.user.UserData;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

@Component
public class Jwt {

    public static final String UID = "uid";
    public static final String PROBATION_AREA_CODES = "probationAreaCodes";

    private final Key key;
    private final int lifetimeSeconds;
    private final JwtParser parser;

    public Jwt(@Value("${jwt.secret}") String secret,
               @Value("${jwt.lifetimeSeconds:300}") int lifetimeSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.lifetimeSeconds = lifetimeSeconds;
        this.parser = Jwts.parserBuilder()
            .setSigningKey(key)
            .build();
    }

    public Optional<Claims> parseToken(String bearerToken) {
        return Optional.ofNullable(parser.parseClaimsJws(bearerToken).getBody());
    }

    public Optional<Claims> parseAuthorizationHeader(String authorizationHeader) {
        return parseToken(authorizationHeader.replace("Bearer ", "").replace("bearer ", ""));
    }

    public String buildToken(UserData userData) {

        Claims claims = Jwts.claims().setSubject(userData.getDistinguishedName());
        claims.put(UID, userData.getUid());
        claims.put(PROBATION_AREA_CODES, userData.getProbationAreaCodes());

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(Date.from(java.time.ZonedDateTime.now().plusSeconds(lifetimeSeconds).toInstant()))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
}
