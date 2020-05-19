package uk.gov.justice.digital.delius.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.data.api.Appointment;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.jpa.filters.AppointmentFilter;
import uk.gov.justice.digital.delius.service.AppointmentService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.time.LocalDate;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentTest {

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private OffenderService offenderService;

    @Captor
    private ArgumentCaptor<AppointmentFilter> appointmentFilterArgumentCaptor;

    @Before
    public void setUp() {
        when(offenderService.offenderIdOfCrn("CRN1")).thenReturn(Optional.of(1L));
        when(offenderService.offenderIdOfNomsNumber("NOMS1")).thenReturn(Optional.of(1L));
        when(offenderService.getOffenderByOffenderId(1L))
            .thenReturn(Optional.of(OffenderDetail.builder().offenderId(1L).build()));
        when(appointmentService.appointmentsFor(eq(1L), any(AppointmentFilter.class)))
            .thenReturn(ImmutableList.of(aAppointment(2L, "Drugs Checkup"), aAppointment(1L, "Accommodation")));

        RestAssuredMockMvc.standaloneSetup(
                new AppointmentController(offenderService, appointmentService)
        );
    }

    @Test
    public void canGetAppointmentsByCrn() {

        Appointment[] appointments = given()
            .when()
            .get("/api/offenders/crn/CRN1/appointments")
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
            .when()
            .get("/api/offenders/nomsNumber/NOMS1/appointments")
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
            .when()
            .get("/api/offenders/offenderId/1/appointments")
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
            .when()
            .get("/api/offenders/crn/notFoundCrn/appointments")
            .then()
            .statusCode(404);

        verify(offenderService).offenderIdOfCrn("notFoundCrn");
    }

    @Test
    public void getAppointmentsForUnknownNomsNumberReturnsNotFound() {
        when(offenderService.offenderIdOfNomsNumber("notFoundNomsNumber")).thenReturn(Optional.empty());

        given()
            .when()
            .get("/api/offenders/nomsNumber/notFoundNomsNumber/appointments")
            .then()
            .statusCode(404);

        verify(offenderService).offenderIdOfNomsNumber("notFoundNomsNumber");
    }

    @Test
    public void getAppointmentsForUnknownOffenderIdReturnsNotFound() {
        when(offenderService.getOffenderByOffenderId(99L)).thenReturn(Optional.empty());

        given()
            .when()
            .get("/api/offenders/offenderId/99/appointments")
            .then()
            .statusCode(404);

        verify(offenderService).getOffenderByOffenderId(99L);
    }

    @Test
    public void appointmentByCrnMustHaveValidJwt() {
        given()
            .when()
            .get("/api/offenders/crn/CRN1/appointments")
            .then()
            .statusCode(401);
    }

    @Test
    public void filterIsPopulatedWhenSuppliedWithCrn() {
        given()
                .params(ImmutableMap.of("from", "2018-11-02", "to", "2018-12-25", "attended", "NOT_RECORDED"))
                .when()
                .get("/api/offenders/crn/CRN1/appointments")
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
                .when()
                .get("/api/offenders/crn/CRN1/appointments")
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
                .params(ImmutableMap.of("from", "2018-11-02", "to", "2018-12-25", "attended", "NOT_RECORDED"))
                .when()
                .get("/api/offenders/nomsNumber/NOMS1/appointments")
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
                .when()
                .get("/api/offenders/nomsNumber/NOMS1/appointments")
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
                .params(ImmutableMap.of("from", "2018-11-02", "to", "2018-12-25", "attended", "NOT_RECORDED"))
                .when()
                .get("/api/offenders/offenderId/1/appointments")
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
                .when()
                .get("/api/offenders/offenderId/1/appointments")
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
}
