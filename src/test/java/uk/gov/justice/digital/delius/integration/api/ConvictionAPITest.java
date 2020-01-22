package uk.gov.justice.digital.delius.integration.api;

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
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.service.ConvictionService;
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
public class ConvictionAPITest {

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConvictionService convictionService;

    @MockBean
    private OffenderService offenderService;

    @Autowired
    private Jwt jwt;

    @Before
    public void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));

        when(offenderService.offenderIdOfCrn("CRN1")).thenReturn(Optional.of(1L));
        when(offenderService.offenderIdOfNomsNumber("NOMS1")).thenReturn(Optional.of(1L));
        when(offenderService.getOffenderByOffenderId(1L))
            .thenReturn(Optional.of(OffenderDetail.builder().offenderId(1L).build()));
        when(convictionService.convictionsFor(1L))
            .thenReturn(ImmutableList.of(aConviction(2L), aConviction(1L)));
    }

    @Test
    public void canGetConvictionsByCrn() {

        Conviction[] convictions = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/crn/CRN1/convictions")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(Conviction[].class);

        assertThat(convictions).hasSize(2);
        assertThat(convictions[0].getConvictionId()).isEqualTo(2L);
        assertThat(convictions[1].getConvictionId()).isEqualTo(1L);
    }

    @Test
    public void canGetConvictionsByNoms() {

        Conviction[] convictions = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/nomsNumber/NOMS1/convictions")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(Conviction[].class);

        assertThat(convictions).hasSize(2);
        assertThat(convictions[0].getConvictionId()).isEqualTo(2L);
        assertThat(convictions[1].getConvictionId()).isEqualTo(1L);
    }

    @Test
    public void canGetConvictionsByOffenderId() {

        Conviction[] convictions = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/offenderId/1/convictions")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(Conviction[].class);

        assertThat(convictions).hasSize(2);
        assertThat(convictions[0].getConvictionId()).isEqualTo(2L);
        assertThat(convictions[1].getConvictionId()).isEqualTo(1L);
    }

    @Test
    public void getConvictionsForUnknownCrnReturnsNotFound() {
        when(offenderService.offenderIdOfCrn("notFoundCrn")).thenReturn(Optional.empty());

        given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/crn/notFoundCrn/convictions")
            .then()
            .statusCode(404);

        verify(offenderService).offenderIdOfCrn("notFoundCrn");
    }

    @Test
    public void getConvictionsForUnknownNomsNumberReturnsNotFound() {
        when(offenderService.offenderIdOfNomsNumber("notFoundNomsNumber")).thenReturn(Optional.empty());

        given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/nomsNumber/notFoundNomsNumber/convictions")
            .then()
            .statusCode(404);

        verify(offenderService).offenderIdOfNomsNumber("notFoundNomsNumber");
    }

    @Test
    public void getConvictionsForUnknownOffenderIdReturnsNotFound() {
        when(offenderService.getOffenderByOffenderId(99L)).thenReturn(Optional.empty());

        given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/offenderId/99/convictions")
            .then()
            .statusCode(404);

        verify(offenderService).getOffenderByOffenderId(99L);
    }

    @Test
    public void convictionByCrnMustHaveValidJwt() {
        given()
            .when()
            .get("offenders/crn/CRN1/convictions")
            .then()
            .statusCode(401);
    }

    private Conviction aConviction(Long id) {
        return Conviction.builder()
            .convictionId(id)
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
