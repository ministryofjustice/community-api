package uk.gov.justice.digital.delius.config;

import com.microsoft.applicationinsights.web.internal.RequestTelemetryContext;
import com.microsoft.applicationinsights.web.internal.ThreadContext;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.groovy.util.Maps;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Import({ClientTrackingInterceptor.class, ClientTrackingConfiguration.class})
@ContextConfiguration(initializers = {ConfigDataApplicationContextInitializer.class})
@ActiveProfiles("dev")
@ExtendWith(SpringExtension.class)
class ClientTrackingConfigurationTest {
    @Autowired
    private ClientTrackingInterceptor clientTrackingInterceptor;

    private static KeyPair keyPair;

    @BeforeAll
    static void staticSetup() throws NoSuchAlgorithmException {
        final var gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        keyPair = gen.generateKeyPair();
    }

    private final MockHttpServletRequest req = new MockHttpServletRequest();
    private final MockHttpServletResponse res = new MockHttpServletResponse();

    @BeforeEach
    void setup() {
        ThreadContext.setRequestTelemetryContext(new RequestTelemetryContext(1L));
    }

    @AfterEach
    void tearDown() {
        ThreadContext.remove();
    }

    @Test
    void shouldAddClientIdAndUserNameToInsightTelemetry() {
        final var token = createJwt("bob");
        req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        clientTrackingInterceptor.preHandle(req, res, "null");
        final var insightTelemetry = ThreadContext.getRequestTelemetryContext().getHttpRequestTelemetry().getProperties();
        assertThat(insightTelemetry).containsExactlyInAnyOrderEntriesOf(Maps.of(
            "username", "bob",
            "clientId", "elite2apiclient"
            )
        );
    }

    @Test
    void shouldAddOnlyClientIdIfUsernameNullToInsightTelemetry() {
        final var token = createJwt(null);
        req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        clientTrackingInterceptor.preHandle(req, res, "null");
        final var insightTelemetry = ThreadContext.getRequestTelemetryContext().getHttpRequestTelemetry().getProperties();
        assertThat(insightTelemetry).containsExactlyInAnyOrderEntriesOf(Maps.of(
            "clientId", "elite2apiclient"
        ));
    }

    @Test
    void shouldCopeWithNoAuthorisation() {
        clientTrackingInterceptor.preHandle(req, res, "null");
        final var insightTelemetry = ThreadContext.getRequestTelemetryContext().getHttpRequestTelemetry().getProperties();
        assertThat(insightTelemetry).isEmpty();
    }

    private String createJwt(final String subject) {
        final var claims = new HashMap<>(Map.<String, Object>of("client_id", "elite2apiclient"));
        Optional.ofNullable(subject).ifPresent((s) -> {
            claims.put("user_name", s);
            claims.put("user_id", s);
        });
        return Jwts.builder()
            .setSubject(subject)
            .addClaims(claims)
            .setExpiration(Date.from(LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC)))
            .signWith(SignatureAlgorithm.RS256, keyPair.getPrivate()).compact();
    }
}
