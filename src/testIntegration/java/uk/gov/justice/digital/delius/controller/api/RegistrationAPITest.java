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
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.Registration;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.RegistrationService;
import uk.gov.justice.digital.delius.user.UserData;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class RegistrationAPITest {

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegistrationService registrationService;

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
        when(registrationService.registrationsFor(1L))
            .thenReturn(ImmutableList.of(
                    aRegistration(2L, "Very High RoSH", "RoSH"),
                    aRegistration(1L, "Risk to Public", "Public Protection")));
    }

    @Test
    public void canGetRegistrationsByCrn() {

        Registration[] registrations = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/crn/CRN1/registrations")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(Registration[].class);

        assertThat(registrations).hasSize(2);
        assertThat(registrations[0].getType().getDescription()).isEqualTo("Very High RoSH");
        assertThat(registrations[0].getRegister().getDescription()).isEqualTo("RoSH");
        assertThat(registrations[1].getType().getDescription()).isEqualTo("Risk to Public");
        assertThat(registrations[1].getRegister().getDescription()).isEqualTo("Public Protection");
    }

    @Test
    public void canGetRegistrationsByNoms() {

        Registration[] registrations = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/nomsNumber/NOMS1/registrations")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(Registration[].class);

        assertThat(registrations).hasSize(2);
        assertThat(registrations[0].getType().getDescription()).isEqualTo("Very High RoSH");
        assertThat(registrations[0].getRegister().getDescription()).isEqualTo("RoSH");
        assertThat(registrations[1].getType().getDescription()).isEqualTo("Risk to Public");
        assertThat(registrations[1].getRegister().getDescription()).isEqualTo("Public Protection");
    }

    @Test
    public void canGetRegistrationsByOffenderId() {

        Registration[] registrations = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/offenderId/1/registrations")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(Registration[].class);

        assertThat(registrations).hasSize(2);
        assertThat(registrations[0].getType().getDescription()).isEqualTo("Very High RoSH");
        assertThat(registrations[0].getRegister().getDescription()).isEqualTo("RoSH");
        assertThat(registrations[1].getType().getDescription()).isEqualTo("Risk to Public");
        assertThat(registrations[1].getRegister().getDescription()).isEqualTo("Public Protection");
    }

    @Test
    public void getRegistrationsForUnknownCrnReturnsNotFound() {
        when(offenderService.offenderIdOfCrn("notFoundCrn")).thenReturn(Optional.empty());

        given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/crn/notFoundCrn/registrations")
            .then()
            .statusCode(404);

        verify(offenderService).offenderIdOfCrn("notFoundCrn");
    }

    @Test
    public void getRegistrationsForUnknownNomsNumberReturnsNotFound() {
        when(offenderService.offenderIdOfNomsNumber("notFoundNomsNumber")).thenReturn(Optional.empty());

        given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/nomsNumber/notFoundNomsNumber/registrations")
            .then()
            .statusCode(404);

        verify(offenderService).offenderIdOfNomsNumber("notFoundNomsNumber");
    }

    @Test
    public void getRegistrationsForUnknownOffenderIdReturnsNotFound() {
        when(offenderService.getOffenderByOffenderId(99L)).thenReturn(Optional.empty());

        given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/offenderId/99/registrations")
            .then()
            .statusCode(404);

        verify(offenderService).getOffenderByOffenderId(99L);
    }

    @Test
    public void registrationByCrnMustHaveValidJwt() {
        given()
            .when()
            .get("offenders/crn/CRN1/registrations")
            .then()
            .statusCode(401);
    }

    private Registration aRegistration(Long id, String description, String register) {
        return Registration.builder()
                .startDate(LocalDate.now())
                .registrationId(id)
                .type(KeyValue.builder().description(description).code("Code for " + description).build())
                .register(KeyValue.builder().description(register).code("Code for " + description).build())
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
