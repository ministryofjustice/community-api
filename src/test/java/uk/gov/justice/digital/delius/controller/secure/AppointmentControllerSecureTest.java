package uk.gov.justice.digital.delius.controller.secure;

import com.google.common.collect.ImmutableList;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.Appointment.Attended;
import uk.gov.justice.digital.delius.data.api.AppointmentDetail;
import uk.gov.justice.digital.delius.data.api.AppointmentOutcome;
import uk.gov.justice.digital.delius.data.api.AppointmentType;
import uk.gov.justice.digital.delius.data.api.AppointmentType.OrderType;
import uk.gov.justice.digital.delius.data.api.RequiredOptional;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.OfficeLocation;
import uk.gov.justice.digital.delius.data.api.StaffHuman;
import uk.gov.justice.digital.delius.jpa.filters.AppointmentFilter;
import uk.gov.justice.digital.delius.service.AppointmentService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AppointmentControllerSecureTest {
    @Mock
    private AppointmentService appointmentService;

    @Mock
    private OffenderService offenderService;

    @Captor
    private ArgumentCaptor<AppointmentFilter> filterCaptor;

    @InjectMocks
    private AppointmentControllerSecure subject;

    @BeforeEach
    public void setUp() {
        RestAssuredMockMvc.standaloneSetup(subject);
    }

    @Test
    public void gettingOffenderAppointmentsByCrn() {
        when(offenderService.offenderIdOfCrn("CRN1")).thenReturn(Optional.of(1L));
        when(appointmentService.appointmentDetailsFor(eq(1L), filterCaptor.capture()))
            .thenReturn(ImmutableList.of(anAppointmentDetail(1L), anAppointmentDetail(2L)));

        final var observed = given()
            .when()
            .get("/secure/offenders/crn/CRN1/appointments?from=2021-05-26&to=2021-06-02&attended=ATTENDED")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(AppointmentDetail[].class);

        assertThat(observed)
            .hasSize(2)
            .extracting("appointmentId", Long.class)
            .containsExactly(1L, 2L);

        assertThat(filterCaptor.getValue())
            .isEqualTo(AppointmentFilter.builder()
                .from(Optional.of(LocalDate.of(2021, 5, 26)))
                .to(Optional.of(LocalDate.of(2021, 6, 2)))
                .attended(Optional.of(Attended.ATTENDED))
                .build());
    }

    @Test
    public void gettingAppointment() {
        final var appointment = anAppointmentDetail(100L);
        when(offenderService.offenderIdOfCrn("CRN1")).thenReturn(Optional.of(1L));
        when(appointmentService.getAppointment(100L, 1L))
            .thenReturn(Optional.of(appointment));

        final var observed = given()
            .when()
            .get("/secure/offenders/crn/CRN1/appointments/100")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(AppointmentDetail.class);

        assertThat(observed)
            .usingRecursiveComparison()
            .ignoringFields("appointmentStart", "appointmentEnd")
            .isEqualTo(appointment);
    }

    private static AppointmentDetail anAppointmentDetail(Long id) {
        return AppointmentDetail.builder()
            .appointmentId(id)
            .appointmentStart(OffsetDateTime.now())
            .appointmentEnd(OffsetDateTime.now().plus(1, ChronoUnit.HOURS))
            .type(AppointmentType.builder()
                .contactType("ABC123")
                .description("Some appointment type")
                .requiresLocation(RequiredOptional.REQUIRED)
                .orderTypes(List.of(OrderType.CJA))
                .build())
            .officeLocation(OfficeLocation.builder()
                .code("ASP_ASH")
                .description("Ashley House Approved Premises")
                .buildingName("Ashley House")
                .buildingNumber("14")
                .streetName("Somerset Street")
                .townCity("Bristol")
                .county("Somerset")
                .postcode("BS2 8NB")
                .build())
            .notes("Some important notes about this appointment.")
            .provider(new KeyValue("P123", "Some provider"))
            .team(new KeyValue("T123", "Some team"))
            .staff(StaffHuman.builder().code("S123").forenames("Alex").surname("Haslehurst").build())
            .sensitive(true)
            .outcome(AppointmentOutcome.builder()
                .code("ABC123")
                .attended(true)
                .complied(true)
                .hoursCredited(1.5)
                .build())
            .build();
    }
}
