package uk.gov.justice.digital.delius.controller.secure;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateResponse;
import uk.gov.justice.digital.delius.jpa.filters.AppointmentFilter;
import uk.gov.justice.digital.delius.service.AppointmentService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(MockitoExtension.class)
public class AppointmentBookingControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private OffenderService offenderService;

    @Captor
    private ArgumentCaptor<AppointmentFilter> appointmentFilterArgumentCaptor;

    @BeforeEach
    public void setUp() {
        RestAssuredMockMvc.standaloneSetup(
                new AppointmentBookingController(appointmentService)
        );
    }

    @Test
    public void createsAppointment() {
        OffsetDateTime now = Instant.now().atZone(ZoneId.of("UTC")).toOffsetDateTime().truncatedTo(ChronoUnit.SECONDS);

        AppointmentCreateRequest appointmentCreateRequest = AppointmentCreateRequest.builder()
            .appointmentStart(now)
            .appointmentEnd(now.plusHours(1))
            .officeLocationCode("CRSSHEF")
            .notes("http://url")
            .context("commissioned-rehabilitation-services")
            .build();
        when(appointmentService.createAppointment("1", 2L, appointmentCreateRequest))
            .thenReturn(AppointmentCreateResponse.builder().appointmentId(3L).build());

        Long appointmentIdResponse = given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(appointmentCreateRequest)
            .when()
            .post("/secure/offenders/crn/1/sentence/2/appointments")
            .then()
            .statusCode(201)
            .extract()
            .body()
            .as(AppointmentCreateResponse.class)
            .getAppointmentId();

        assertThat(appointmentIdResponse).isEqualTo(3L);
    }
}
