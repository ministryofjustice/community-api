package uk.gov.justice.digital.delius.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.Offence;
import uk.gov.justice.digital.delius.data.api.OffenceDetail;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.service.OffenceService;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.user.UserData;

import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class OffencesAPITest {

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OffenceService offenceService;

    @MockBean
    private OffenderService offenderService;

    @Autowired
    private Jwt jwt;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));

        when(offenderService.offenderIdOfCrn("crn1")).thenReturn(Optional.of(1L));
        when(offenderService.offenderIdOfNomsNumber("noms1")).thenReturn(Optional.of(1L));
        when(offenderService.getOffenderByOffenderId(1L))
            .thenReturn(Optional.of(OffenderDetail.builder().offenderId(1L).build()));
        when(offenceService.offencesFor(1L))
            .thenReturn(ImmutableList.of(anOffence(1L, "Fraud"), anOffence(2L, "Perjury")));
    }

    @Test
    public void canGetOffencesByCrn() {

        Offence[] offences = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/crn/crn1/offences")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(Offence[].class);

        assertThat(offences).hasSize(2);
        assertThat(offences[0].getOffenceId()).isEqualTo("1");
        assertThat(offences[1].getOffenceId()).isEqualTo("2");
        assertThat(offences[0].getDetail().getDescription()).isEqualTo("Fraud");
        assertThat(offences[1].getDetail().getDescription()).isEqualTo("Perjury");
    }

    @Test
    public void canGetOffencesByNoms() {

        Offence[] offences = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/nomsNumber/noms1/offences")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(Offence[].class);

        assertThat(offences).hasSize(2);
        assertThat(offences[0].getOffenceId()).isEqualTo("1");
        assertThat(offences[1].getOffenceId()).isEqualTo("2");
        assertThat(offences[0].getDetail().getDescription()).isEqualTo("Fraud");
        assertThat(offences[1].getDetail().getDescription()).isEqualTo("Perjury");
    }

    @Test
    public void canGetOffencesByOffenderId() {

        Offence[] offences = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/offenderId/1/offences")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(Offence[].class);

        assertThat(offences).hasSize(2);
        assertThat(offences[0].getOffenceId()).isEqualTo("1");
        assertThat(offences[1].getOffenceId()).isEqualTo("2");
        assertThat(offences[0].getDetail().getDescription()).isEqualTo("Fraud");
        assertThat(offences[1].getDetail().getDescription()).isEqualTo("Perjury");
    }

    @Test
    public void getOffencesForUnknownCrnReturnsNotFound() {
        when(offenderService.offenderIdOfCrn("notFoundCrn")).thenReturn(Optional.empty());

        given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/crn/notFoundCrn/offences")
            .then()
            .statusCode(404);

        verify(offenderService).offenderIdOfCrn("notFoundCrn");
    }

    @Test
    public void getOffencesForUnknownNomsNumberReturnsNotFound() {
        when(offenderService.offenderIdOfNomsNumber("notFoundNomsNumber")).thenReturn(Optional.empty());

        given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/nomsNumber/notFoundNomsNumber/offences")
            .then()
            .statusCode(404);

        verify(offenderService).offenderIdOfNomsNumber("notFoundNomsNumber");
    }

    @Test
    public void getOffencesForUnknownOffenderIdReturnsNotFound() {
        when(offenderService.getOffenderByOffenderId(99L)).thenReturn(Optional.empty());

        given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/offenderId/99/offences")
            .then()
            .statusCode(404);

        verify(offenderService).getOffenderByOffenderId(99L);
    }

    @Test
    public void offencesByCrnMustHaveValidJwt() {
        given()
            .when()
            .get("offenders/crn/crn1/offences")
            .then()
            .statusCode(401);
    }

    private Offence anOffence(Long id, String description) {
        return Offence.builder()
            .offenceId(id.toString())
            .detail(OffenceDetail.builder().code("001").description(description).build())
            .build();
    }

    private String aValidToken() {
        return aValidTokenFor(UUID.randomUUID().toString());
    }

    private String aValidTokenFor(String distinguishedName) {
        return "Bearer " + jwt.buildToken(UserData.builder()
                .distinguishedName(distinguishedName)
                .uid("bobby.davro").build());
    }
}
