package uk.gov.justice.digital.delius;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.PersonalCircumstance;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.PersonalCircumstanceService;
import uk.gov.justice.digital.delius.user.UserData;

import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class PersonalCircumstancesAPITest {

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PersonalCircumstanceService personalCircumstanceService;

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

        when(offenderService.offenderIdOfCrn("CRN1")).thenReturn(Optional.of(1L));
        when(offenderService.offenderIdOfNomsNumber("NOMS1")).thenReturn(Optional.of(1L));
        when(offenderService.getOffenderByOffenderId(1L))
            .thenReturn(Optional.of(OffenderDetail.builder().offenderId(1L).build()));
        when(personalCircumstanceService.personalCircumstancesFor(1L))
            .thenReturn(ImmutableList.of(aPersonalCircumstance(2L, "Benefit", "Universal Benefit"), aPersonalCircumstance(1L, "Accommodation", "Approved Premises")));
    }

    @Test
    public void canGetPersonalCircumstancesByCrn() {

        PersonalCircumstance[] personalCircumstances = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/crn/CRN1/personalCircumstances")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(PersonalCircumstance[].class);

        assertThat(personalCircumstances).hasSize(2);
        assertThat(personalCircumstances[0].getType().getDescription()).isEqualTo("Benefit");
        assertThat(personalCircumstances[0].getSubType().getDescription()).isEqualTo("Universal Benefit");
        assertThat(personalCircumstances[1].getType().getDescription()).isEqualTo("Accommodation");
        assertThat(personalCircumstances[1].getSubType().getDescription()).isEqualTo("Approved Premises");
    }

    @Test
    public void canGetPersonalCircumstancesByNoms() {

        PersonalCircumstance[] personalCircumstances = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/nomsNumber/NOMS1/personalCircumstances")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(PersonalCircumstance[].class);

        assertThat(personalCircumstances).hasSize(2);
        assertThat(personalCircumstances[0].getType().getDescription()).isEqualTo("Benefit");
        assertThat(personalCircumstances[0].getSubType().getDescription()).isEqualTo("Universal Benefit");
        assertThat(personalCircumstances[1].getType().getDescription()).isEqualTo("Accommodation");
        assertThat(personalCircumstances[1].getSubType().getDescription()).isEqualTo("Approved Premises");
    }

    @Test
    public void canGetPersonalCircumstancesByOffenderId() {

        PersonalCircumstance[] personalCircumstances = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/offenderId/1/personalCircumstances")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(PersonalCircumstance[].class);

        assertThat(personalCircumstances).hasSize(2);
        assertThat(personalCircumstances[0].getType().getDescription()).isEqualTo("Benefit");
        assertThat(personalCircumstances[0].getSubType().getDescription()).isEqualTo("Universal Benefit");
        assertThat(personalCircumstances[1].getType().getDescription()).isEqualTo("Accommodation");
        assertThat(personalCircumstances[1].getSubType().getDescription()).isEqualTo("Approved Premises");
    }

    @Test
    public void getPersonalCircumstancesForUnknownCrnReturnsNotFound() {
        when(offenderService.offenderIdOfCrn("notFoundCrn")).thenReturn(Optional.empty());

        given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/crn/notFoundCrn/personalCircumstances")
            .then()
            .statusCode(404);

        verify(offenderService).offenderIdOfCrn("notFoundCrn");
    }

    @Test
    public void getPersonalCircumstancesForUnknownNomsNumberReturnsNotFound() {
        when(offenderService.offenderIdOfNomsNumber("notFoundNomsNumber")).thenReturn(Optional.empty());

        given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/nomsNumber/notFoundNomsNumber/personalCircumstances")
            .then()
            .statusCode(404);

        verify(offenderService).offenderIdOfNomsNumber("notFoundNomsNumber");
    }

    @Test
    public void getPersonalCircumstancesForUnknownOffenderIdReturnsNotFound() {
        when(offenderService.getOffenderByOffenderId(99L)).thenReturn(Optional.empty());

        given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/offenderId/99/personalCircumstances")
            .then()
            .statusCode(404);

        verify(offenderService).getOffenderByOffenderId(99L);
    }

    @Test
    public void personalCircumstanceByCrnMustHaveValidJwt() {
        given()
            .when()
            .get("offenders/crn/CRN1/personalCircumstances")
            .then()
            .statusCode(401);
    }

    private PersonalCircumstance aPersonalCircumstance(Long id, String typeDescription, String subTypeDescription) {
        return PersonalCircumstance.builder()
            .personalCircumstanceId(id)
            .type(KeyValue.builder().code("X").description(typeDescription).build())
            .subType(KeyValue.builder().code("X").description(subTypeDescription).build())
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