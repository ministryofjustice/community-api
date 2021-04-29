package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.ReplaceOperation;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateResponse;
import uk.gov.justice.digital.delius.data.api.AppointmentUpdateResponse;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentCreateRequest;
import uk.gov.justice.digital.delius.jpa.filters.AppointmentFilter;
import uk.gov.justice.digital.delius.service.AppointmentService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.stream.StreamSupport;

import static com.fasterxml.jackson.databind.node.TextNode.valueOf;
import static com.github.fge.jackson.jsonpointer.JsonPointer.of;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
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
            .requirementId(123456L)
            .contactType("CRSAPT")
            .appointmentStart(now)
            .appointmentEnd(now.plusHours(1))
            .officeLocationCode("CRSSHEF")
            .notes("http://url")
            .providerCode("CRS")
            .staffCode("CRSUAT")
            .teamCode("CRSUATU")
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

    @Test
    public void createsAppointmentUsingContextlessClientEndpoint() {
        OffsetDateTime now = Instant.now().atZone(ZoneId.of("UTC")).toOffsetDateTime().truncatedTo(ChronoUnit.SECONDS);

        ContextlessAppointmentCreateRequest appointmentCreateRequest = ContextlessAppointmentCreateRequest.builder()
            .appointmentStart(now)
            .appointmentEnd(now.plusHours(1))
            .officeLocationCode("CRSSHEF")
            .notes("http://url")
            .build();
        when(appointmentService.createAppointment("1", 2L, "commissioned-rehabilitation-services", appointmentCreateRequest))
            .thenReturn(AppointmentCreateResponse.builder().appointmentId(3L).build());

        Long appointmentIdResponse = given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(appointmentCreateRequest)
            .when()
            .post("/secure/offenders/crn/1/sentence/2/appointments/context/commissioned-rehabilitation-services")
            .then()
            .statusCode(201)
            .extract()
            .body()
            .as(AppointmentCreateResponse.class)
            .getAppointmentId();

        assertThat(appointmentIdResponse).isEqualTo(3L);
    }

    @Test
    public void patchesAppointmentUsingContextlessClientEndpoint() {

        JsonPatch jsonPatch = new JsonPatch(asList(new ReplaceOperation(of("attended"), valueOf("LATE"))));

        when(appointmentService.patchAppointment(eq("1"), eq(2L), eq("commissioned-rehabilitation-services"),
            ArgumentMatchers.argThat(patch -> getFieldValue("/attended", jsonPatch).equals("LATE"))))
            .thenReturn(AppointmentUpdateResponse.builder().appointmentId(2L).build());

        Long appointmentIdResponse = given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(jsonPatch)
            .when()
            .patch("/secure/offenders/crn/1/appointments/2/context/commissioned-rehabilitation-services")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(AppointmentUpdateResponse.class)
            .getAppointmentId();

        assertThat(appointmentIdResponse).isEqualTo(2L);
    }

    private String getFieldValue(String path, JsonPatch jsonPatch) {
        final var objectMapper = new ObjectMapper();
        return StreamSupport.stream(objectMapper.convertValue(jsonPatch, JsonNode.class).spliterator(), false)
            .filter(node -> node.path("path").asText().equals(path))
            .map(node -> node.path("value"))
            .findFirst()
            .map(JsonNode::asText)
            .orElseThrow(() -> new IllegalStateException("Expected value"));
    }
}
