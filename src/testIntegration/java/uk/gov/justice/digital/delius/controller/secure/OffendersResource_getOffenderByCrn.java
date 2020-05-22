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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.OffenderDetailSummary;
import uk.gov.justice.digital.delius.data.api.OffenderManager;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
public class OffendersResource_getOffenderByCrn {

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
    public void canGetOffenderDetailsByCrn() {
        final var offenderDetail = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X320741/all")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(OffenderDetail.class);

      assertThat(offenderDetail.getOtherIds().getCrn()).isEqualTo("X320741");
      final var offenderManager = offenderDetail.getOffenderManagers().stream().filter(OffenderManager::getActive).findAny();
      assertThat(offenderManager).isPresent();
    }

  @Test
  public void canGetOffenderSummaryByCrn() {
    final var offenderDetail = given()
      .auth()
      .oauth2(validOauthToken)
      .contentType(APPLICATION_JSON_VALUE)
      .when()
      .get("/offenders/crn/X320741")
      .then()
      .statusCode(200)
      .extract()
      .body()
      .as(OffenderDetailSummary.class);

    assertThat(offenderDetail.getOtherIds().getCrn()).isEqualTo("X320741");
  }

    @Test
    public void getOffenderSummaryByCrn_offenderNotFound_returnsNotFound() {
        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X777777")
                .then()
                .statusCode(404);
    }

    @Test
    public void getOffenderDetailsByCrn_offenderNotFound_returnsNotFound() {
      given()
        .auth()
        .oauth2(validOauthToken)
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .get("/offenders/crn/X777777/all")
        .then()
        .statusCode(404);
    }

}
