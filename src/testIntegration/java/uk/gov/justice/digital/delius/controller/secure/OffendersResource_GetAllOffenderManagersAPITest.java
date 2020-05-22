package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
public class OffendersResource_GetAllOffenderManagersAPITest {

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${test.token.good}")
    private String validOauthToken;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
    }

    @Test
    public void canGetAllOffenderManagersByNOMSNumber() {
        final var offenderManagers = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/G9542VP/allOffenderManagers")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager[].class);

        assertThat(offenderManagers).hasSize(2);

        final var communityOffenderManager = Stream.of(offenderManagers).filter(not(CommunityOrPrisonOffenderManager::getIsPrisonOffenderManager)).findAny().orElseThrow();
        final var prisonOffenderManager = Stream.of(offenderManagers).filter(CommunityOrPrisonOffenderManager::getIsPrisonOffenderManager).findAny().orElseThrow();

        assertThat(communityOffenderManager.getIsResponsibleOfficer()).isFalse();
        assertThat(communityOffenderManager.getIsUnallocated()).isTrue();
        assertThat(communityOffenderManager.getProbationArea()).isNotNull();
        assertThat(communityOffenderManager.getProbationArea().getInstitution()).isNull();
        assertThat(communityOffenderManager.getStaff()).isNotNull();
        assertThat(communityOffenderManager.getTeam()).isNotNull();
        assertThat(communityOffenderManager.getStaffCode()).isEqualTo("N02AAMU");

        assertThat(prisonOffenderManager.getIsResponsibleOfficer()).isTrue();
        assertThat(prisonOffenderManager.getIsUnallocated()).isFalse();
        assertThat(prisonOffenderManager.getProbationArea()).isNotNull();
        assertThat(prisonOffenderManager.getProbationArea().getInstitution()).isNotNull();
        assertThat(prisonOffenderManager.getStaff()).isNotNull();
        assertThat(prisonOffenderManager.getTeam()).isNotNull();
        assertThat(prisonOffenderManager.getStaffCode()).isEqualTo("BWIA010");
    }

    @Test
    public void getAllOffenderManagersByNOMSNumberReturn404WhenOffenderDoesNotExist() {
        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/DOESNOTEXIST/allOffenderManagers")
                .then()
                .statusCode(404);
    }

}
