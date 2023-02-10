package uk.gov.justice.digital.delius;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
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

        if (parameters.getRoles() != null && !parameters.getRoles().isEmpty())
            claims.put("authorities", parameters.getRoles());

        if (parameters.getScope() != null && !parameters.getScope().isEmpty())
            claims.put("scope", parameters.getScope());

        final var builder = Jwts.builder().setId(UUID.randomUUID().toString());
        Optional.ofNullable(parameters.getClientId()).ifPresent(clientId -> {
            claims.put("client_id", clientId);
            builder.setSubject(clientId);
        });
        Optional.ofNullable(parameters.getUsername()).ifPresent(username -> {
            claims.put("user_name", username);
            builder.setSubject(username);
            Optional.ofNullable(parameters.getName()).ifPresent(name -> claims.put("name", name));
        });
        Optional.ofNullable(parameters.getUserId()).ifPresent(userId -> claims.put("user_id", userId));

        return builder.addClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + parameters.getExpiryTime().toMillis()))
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
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
}