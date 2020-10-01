package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.digital.delius.JwtAuthenticationHelper;
import uk.gov.justice.digital.delius.JwtParameters;
import uk.gov.justice.digital.delius.JwtParameters.JwtParametersBuilder;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.user.UserData;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
public class IntegrationTestBase {
    @Autowired
    protected JwtAuthenticationHelper jwtAuthenticationHelper;
    @LocalServerPort
    int port;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private Jwt jwt;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    protected String createJwt(final String... roles) {
        return jwtAuthenticationHelper.createJwt(
                createJwtBuilder(roles)
                        .clientId("system-client-id")
                        .build());
    }

    protected JwtParametersBuilder createJwtBuilder(final String... roles) {
        return JwtParameters.builder()
                .roles(List.of(roles))
                .scope(Arrays.asList("read", "write"))
                .expiryTime(Duration.ofDays(1));
    }

    protected String tokenWithRoleCommunity() {
        return createJwt("ROLE_COMMUNITY");
    }

    protected String tokenWithRoleCommunityAndCustodyUpdate() {
        return createJwt("ROLE_COMMUNITY", "ROLE_COMMUNITY_CUSTODY_UPDATE");
    }

    protected String writeValueAsString(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    protected String legacyToken() {
        return aValidTokenFor(UUID.randomUUID().toString());
    }

    private String aValidTokenFor(String distinguishedName) {
        return "Bearer " + jwt.buildToken(UserData.builder()
                .distinguishedName(distinguishedName)
                .uid("bobby.davro").build());
    }

    protected String tokenWithRoleCommunityAndUser(final String username) {
        return createJwtWithUsername(username,"ROLE_COMMUNITY");
    }

    protected String createJwtWithUsername(final String username, final String... roles ) {
        return jwtAuthenticationHelper.createJwt(
                createJwtBuilder(roles)
                        .username(username)
                        .clientId("system-client-id")
                        .build());
    }

}
