package uk.gov.justice.digital.delius.controller.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
public class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private OffenderService offenderService;

    @Captor
    private ArgumentCaptor<AppointmentFilter> appointmentFilterArgumentCaptor;

    @BeforeEach
    public void setUp() {
        RestAssuredMockMvc.standaloneSetup(
                new AppointmentController(offenderService, appointmentService)
        );
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
    public void filterIsPopulatedWhenSuppliedWithOffenderId() {
        when(offenderService.getOffenderByOffenderId(1L))
                .thenReturn(Optional.of(OffenderDetail.builder().offenderId(1L).build()));
        when(appointmentService.appointmentsFor(eq(1L), any(AppointmentFilter.class)))
                .thenReturn(ImmutableList.of(aAppointment(2L, "Drugs Checkup"), aAppointment(1L, "Accommodation")));

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
        when(offenderService.getOffenderByOffenderId(1L))
                .thenReturn(Optional.of(OffenderDetail.builder().offenderId(1L).build()));
        when(appointmentService.appointmentsFor(eq(1L), any(AppointmentFilter.class)))
                .thenReturn(ImmutableList.of(aAppointment(2L, "Drugs Checkup"), aAppointment(1L, "Accommodation")));

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
