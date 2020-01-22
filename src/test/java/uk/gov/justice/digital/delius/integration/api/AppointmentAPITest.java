package uk.gov.justice.digital.delius.integration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.Appointment;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.jpa.filters.AppointmentFilter;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.service.AppointmentService;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.user.UserData;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class AppointmentAPITest {

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private OffenderService offenderService;

    @Autowired
    private Jwt jwt;

    @Captor
    private ArgumentCaptor<AppointmentFilter> appointmentFilterArgumentCaptor;

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
        when(appointmentService.appointmentsFor(eq(1L), any(AppointmentFilter.class)))
            .thenReturn(ImmutableList.of(aAppointment(2L, "Drugs Checkup"), aAppointment(1L, "Accommodation")));
    }

    @Test
    public void canGetAppointmentsByCrn() {

        Appointment[] appointments = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/crn/CRN1/appointments")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(Appointment[].class);

        assertThat(appointments).hasSize(2);
        assertThat(appointments[0].getAppointmentType().getDescription()).isEqualTo("Drugs Checkup");
        assertThat(appointments[1].getAppointmentType().getDescription()).isEqualTo("Accommodation");
    }

    @Test
    public void canGetAppointmentsByNoms() {

        Appointment[] appointments = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/nomsNumber/NOMS1/appointments")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(Appointment[].class);

        assertThat(appointments).hasSize(2);
        assertThat(appointments[0].getAppointmentType().getDescription()).isEqualTo("Drugs Checkup");
        assertThat(appointments[1].getAppointmentType().getDescription()).isEqualTo("Accommodation");
    }

    @Test
    public void canGetAppointmentsByOffenderId() {

        Appointment[] appointments = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/offenderId/1/appointments")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(Appointment[].class);

        assertThat(appointments).hasSize(2);
        assertThat(appointments[0].getAppointmentType().getDescription()).isEqualTo("Drugs Checkup");
        assertThat(appointments[1].getAppointmentType().getDescription()).isEqualTo("Accommodation");
    }

    @Test
    public void getAppointmentsForUnknownCrnReturnsNotFound() {
        when(offenderService.offenderIdOfCrn("notFoundCrn")).thenReturn(Optional.empty());

        given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/crn/notFoundCrn/appointments")
            .then()
            .statusCode(404);

        verify(offenderService).offenderIdOfCrn("notFoundCrn");
    }

    @Test
    public void getAppointmentsForUnknownNomsNumberReturnsNotFound() {
        when(offenderService.offenderIdOfNomsNumber("notFoundNomsNumber")).thenReturn(Optional.empty());

        given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/nomsNumber/notFoundNomsNumber/appointments")
            .then()
            .statusCode(404);

        verify(offenderService).offenderIdOfNomsNumber("notFoundNomsNumber");
    }

    @Test
    public void getAppointmentsForUnknownOffenderIdReturnsNotFound() {
        when(offenderService.getOffenderByOffenderId(99L)).thenReturn(Optional.empty());

        given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/offenderId/99/appointments")
            .then()
            .statusCode(404);

        verify(offenderService).getOffenderByOffenderId(99L);
    }

    @Test
    public void appointmentByCrnMustHaveValidJwt() {
        given()
            .when()
            .get("offenders/crn/CRN1/appointments")
            .then()
            .statusCode(401);
    }

    @Test
    public void filterIsPopulatedWhenSuppliedWithCrn() {
        given()
                .header("Authorization", aValidToken())
                .params(ImmutableMap.of("from", "2018-11-02", "to", "2018-12-25", "attended", "NOT_RECORDED"))
                .when()
                .get("offenders/crn/CRN1/appointments")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Appointment[].class);

        verify(appointmentService).appointmentsFor(eq(1L), appointmentFilterArgumentCaptor.capture());

        assertThat(appointmentFilterArgumentCaptor.getValue().getAttended().orElse(null)).isEqualTo(Appointment.Attended.NOT_RECORDED);
        assertThat(appointmentFilterArgumentCaptor.getValue().getFrom().orElse(null)).isEqualTo(LocalDate.of(2018, 11, 2));
        assertThat(appointmentFilterArgumentCaptor.getValue().getTo().orElse(null)).isEqualTo(LocalDate.of(2018, 12, 25));


    }

    @Test
    public void filterIsOptionalWhenSuppliedWithCrn() {
        given()
                .header("Authorization", aValidToken())
                .when()
                .get("offenders/crn/CRN1/appointments")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Appointment[].class);

        verify(appointmentService).appointmentsFor(eq(1L), appointmentFilterArgumentCaptor.capture());

        assertThat(appointmentFilterArgumentCaptor.getValue().getAttended().isPresent()).isFalse();
        assertThat(appointmentFilterArgumentCaptor.getValue().getFrom().isPresent()).isFalse();
        assertThat(appointmentFilterArgumentCaptor.getValue().getTo().isPresent()).isFalse();
    }

    @Test
    public void filterIsPopulatedWhenSuppliedWithNoms() {
        given()
                .header("Authorization", aValidToken())
                .params(ImmutableMap.of("from", "2018-11-02", "to", "2018-12-25", "attended", "NOT_RECORDED"))
                .when()
                .get("offenders/nomsNumber/NOMS1/appointments")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Appointment[].class);

        verify(appointmentService).appointmentsFor(eq(1L), appointmentFilterArgumentCaptor.capture());

        assertThat(appointmentFilterArgumentCaptor.getValue().getAttended().orElse(null)).isEqualTo(Appointment.Attended.NOT_RECORDED);
        assertThat(appointmentFilterArgumentCaptor.getValue().getFrom().orElse(null)).isEqualTo(LocalDate.of(2018, 11, 2));
        assertThat(appointmentFilterArgumentCaptor.getValue().getTo().orElse(null)).isEqualTo(LocalDate.of(2018, 12, 25));


    }

    @Test
    public void filterIsOptionalWhenSuppliedWithNoms() {
        given()
                .header("Authorization", aValidToken())
                .when()
                .get("offenders/nomsNumber/NOMS1/appointments")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Appointment[].class);

        verify(appointmentService).appointmentsFor(eq(1L), appointmentFilterArgumentCaptor.capture());

        assertThat(appointmentFilterArgumentCaptor.getValue().getAttended().isPresent()).isFalse();
        assertThat(appointmentFilterArgumentCaptor.getValue().getFrom().isPresent()).isFalse();
        assertThat(appointmentFilterArgumentCaptor.getValue().getTo().isPresent()).isFalse();
    }

    @Test
    public void filterIsPopulatedWhenSuppliedWithOffenderId() {
        given()
                .header("Authorization", aValidToken())
                .params(ImmutableMap.of("from", "2018-11-02", "to", "2018-12-25", "attended", "NOT_RECORDED"))
                .when()
                .get("offenders/offenderId/1/appointments")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Appointment[].class);

        verify(appointmentService).appointmentsFor(eq(1L), appointmentFilterArgumentCaptor.capture());

        assertThat(appointmentFilterArgumentCaptor.getValue().getAttended().orElse(null)).isEqualTo(Appointment.Attended.NOT_RECORDED);
        assertThat(appointmentFilterArgumentCaptor.getValue().getFrom().orElse(null)).isEqualTo(LocalDate.of(2018, 11, 2));
        assertThat(appointmentFilterArgumentCaptor.getValue().getTo().orElse(null)).isEqualTo(LocalDate.of(2018, 12, 25));


    }

    @Test
    public void filterIsOptionalWhenSuppliedWithOffenderId() {
        given()
                .header("Authorization", aValidToken())
                .when()
                .get("offenders/offenderId/1/appointments")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Appointment[].class);

        verify(appointmentService).appointmentsFor(eq(1L), appointmentFilterArgumentCaptor.capture());

        assertThat(appointmentFilterArgumentCaptor.getValue().getAttended().isPresent()).isFalse();
        assertThat(appointmentFilterArgumentCaptor.getValue().getFrom().isPresent()).isFalse();
        assertThat(appointmentFilterArgumentCaptor.getValue().getTo().isPresent()).isFalse();
    }

    private Appointment aAppointment(Long id, String typeDescription) {
        return Appointment.builder()
            .appointmentId(id)
            .appointmentType(KeyValue.builder().code("X").description(typeDescription).build())
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
