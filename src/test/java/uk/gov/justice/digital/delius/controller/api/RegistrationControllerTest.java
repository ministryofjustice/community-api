package uk.gov.justice.digital.delius.controller.api;

import com.google.common.collect.ImmutableList;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.Registration;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.RegistrationService;

import java.time.LocalDate;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RegistrationControllerTest {

    @Mock
    private RegistrationService registrationService;

    @Mock
    private OffenderService offenderService;

    @BeforeEach
    public void setUp() {
        RestAssuredMockMvc.standaloneSetup(
                new RegistrationController(offenderService, registrationService)
        );
    }

    @Test
    public void canGetRegistrationsByCrn() {
        when(offenderService.offenderIdOfCrn("CRN1")).thenReturn(Optional.of(1L));
        when(registrationService.registrationsFor(1L))
                .thenReturn(ImmutableList.of(
                        aRegistration(2L, "Very High RoSH", "RoSH"),
                        aRegistration(1L, "Risk to Public", "Public Protection")));

        Registration[] registrations = given()
            .when()
            .get("/api/offenders/crn/CRN1/registrations")
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
        when(offenderService.getOffenderByOffenderId(1L))
                .thenReturn(Optional.of(OffenderDetail.builder().offenderId(1L).build()));
        when(registrationService.registrationsFor(1L))
                .thenReturn(ImmutableList.of(
                        aRegistration(2L, "Very High RoSH", "RoSH"),
                        aRegistration(1L, "Risk to Public", "Public Protection")));

        Registration[] registrations = given()
            .when()
            .get("/api/offenders/offenderId/1/registrations")
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
            .when()
            .get("/api/offenders/crn/notFoundCrn/registrations")
            .then()
            .statusCode(404);

        verify(offenderService).offenderIdOfCrn("notFoundCrn");
    }

    @Test
    public void getRegistrationsForUnknownOffenderIdReturnsNotFound() {
        when(offenderService.getOffenderByOffenderId(99L)).thenReturn(Optional.empty());

        given()
            .when()
            .get("/api/offenders/offenderId/99/registrations")
            .then()
            .statusCode(404);

        verify(offenderService).getOffenderByOffenderId(99L);
    }


    private Registration aRegistration(Long id, String description, String register) {
        return Registration.builder()
                .startDate(LocalDate.now())
                .registrationId(id)
                .type(KeyValue.builder().description(description).code("Code for " + description).build())
                .register(KeyValue.builder().description(register).code("Code for " + description).build())
                .build();
    }

}
