package uk.gov.justice.digital.delius;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationHelper {
    private final KeyPair keyPair;

    public JwtAuthenticationHelper(@Value("${jwt.signing.key.pair}") final String privateKeyPair,
                                   @Value("${jwt.keystore.password}") final String keystorePassword,
                                   @Value("${jwt.keystore.alias:elite2api}") final String keystoreAlias) {
        keyPair = getKeyPair(new ByteArrayResource(Base64.decodeBase64(privateKeyPair)), keystoreAlias, keystorePassword.toCharArray());
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey((RSAPublicKey) keyPair.getPublic()).build();
    }

    public String createJwt(final JwtParameters parameters) {

        final HashMap<String, Object> claims = new HashMap<>();
        claims.put("user_name", parameters.getUsername());
        claims.put("client_id", parameters.getClientId());
        claims.put("user_id", parameters.getUserId());

        if (parameters.getRoles() != null && !parameters.getRoles().isEmpty())
            claims.put("authorities", parameters.getRoles());

        if (parameters.getScope() != null && !parameters.getScope().isEmpty())
            claims.put("scope", parameters.getScope());

        return Jwts.builder()
                .setId(parameters.getJwtId())
                .setSubject(parameters.getUsername())
                .addClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + parameters.getExpiryTime().toMillis()))
                .signWith(SignatureAlgorithm.RS256, keyPair.getPrivate())
                .compact();
    }

    private KeyPair getKeyPair(final Resource resource, final String alias, final char[] password) {
        try (InputStream inputStream = resource.getInputStream()) {
            final var store = KeyStore.getInstance("jks");
            store.load(inputStream, password);
            final var key = (RSAPrivateCrtKey) store.getKey(alias, password);
            final var spec = new RSAPublicKeySpec(key.getModulus(), key.getPublicExponent());
            final var publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
            return new KeyPair(publicKey, key);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load keys from store: " + resource, e);
        }
    }

    @Data
    @Builder(toBuilder = true)
    public class JwtParameters {
        private String username;
        private String userId;
        @Builder.Default
        private List<String> scope = List.of();
        @Builder.Default
        private List<String> roles = List.of();
        @Builder.Default
        private Duration expiryTime = Duration.ofHours(1L);
        @Builder.Default
        private String jwtId = UUID.randomUUID().toString();
        @Builder.Default
        private String clientId = "community-api-client";
    }
}