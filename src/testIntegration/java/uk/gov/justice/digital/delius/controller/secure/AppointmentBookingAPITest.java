package uk.gov.justice.digital.delius.controller.secure;

import io.restassured.RestAssured;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import uk.gov.justice.digital.delius.JwtAuthenticationHelper;
import uk.gov.justice.digital.delius.JwtParameters;
import uk.gov.justice.digital.delius.controller.wiremock.DeliusApiExtension;
import uk.gov.justice.digital.delius.controller.wiremock.DeliusApiMockServer;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.AppointmentRelocateRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentOutcomeRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentRescheduleRequest;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class AppointmentBookingAPITest extends IntegrationTestBase {

    private static final DeliusApiMockServer deliusApiMockServer = new DeliusApiMockServer(7999);

    @RegisterExtension
    static DeliusApiExtension deliusExtension = new DeliusApiExtension(deliusApiMockServer);

    @LocalServerPort
    int port;

    @Autowired
    protected JwtAuthenticationHelper jwtAuthenticationHelper;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
    }

    @Test
    public void shouldReturnOKAfterCreatingANewContact() {

        deliusApiMockServer.stubPostContactToDeliusApi();

        final var token = createJwt("bob", Collections.singletonList("ROLE_COMMUNITY_INTERVENTIONS_UPDATE"));

        given()
            .when()
            .auth().oauth2(token)
            .contentType(String.valueOf(ContentType.APPLICATION_JSON))
            .body(writeValueAsString(anAppointmentCreateRequest("CRSAPT")))
            .post("offenders/crn/X320741/sentence/2500295343/appointments")
            .then()
            .assertThat()
            .statusCode(HttpStatus.CREATED.value())
            .body("appointmentId", equalTo(2500029015L))
            .body("type", equalTo("CRSAPT"))
            .body("typeDescription", equalTo("Appointment with CRS Provider (NS)"))
            .body("appointmentStart", equalTo("2021-03-01T13:01:02Z"))
            .body("appointmentEnd", equalTo("2021-03-01T14:03:04Z"))
            .body("sensitive", equalTo(true));
    }

    @Test
    public void whenAttemptingToCreateAppointmentFromNonAppointmentContactType() {

        deliusApiMockServer.stubPostContactToDeliusApi();

        final var token = createJwt("bob", Collections.singletonList("ROLE_COMMUNITY_INTERVENTIONS_UPDATE"));

        given()
            .when()
            .auth().oauth2(token)
            .contentType(String.valueOf(ContentType.APPLICATION_JSON))
            .body(writeValueAsString(anAppointmentCreateRequest("C062")))
            .post("offenders/crn/X320741/sentence/2500295343/appointments")
            .then()
            .assertThat()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void whenAttemptingToCreateAppointmentFromMissingContactType() {

        deliusApiMockServer.stubPostContactToDeliusApi();

        final var token = createJwt("bob", Collections.singletonList("ROLE_COMMUNITY_INTERVENTIONS_UPDATE"));

        given()
            .when()
            .auth().oauth2(token)
            .contentType(String.valueOf(ContentType.APPLICATION_JSON))
            .body(writeValueAsString(anAppointmentCreateRequest("MISSING_CONTACT_TYPE")))
            .post("offenders/crn/X320741/sentence/2500295343/appointments")
            .then()
            .assertThat()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldReturnOKAfterCreatingANewContactUsingContextlessClientEndpoint() {

        deliusApiMockServer.stubPostContactToDeliusApi();

        final var token = createJwt("bob", Collections.singletonList("ROLE_COMMUNITY_INTERVENTIONS_UPDATE"));

        given()
            .when()
            .auth().oauth2(token)
            .contentType(String.valueOf(ContentType.APPLICATION_JSON))
            .body(writeValueAsString(ContextlessAppointmentCreateRequest.builder()
                .contractType("ACC")
                .referralStart(OffsetDateTime.of(2019, 9, 2, 0, 0, 0, 0, ZoneOffset.UTC))
                .appointmentStart(OffsetDateTime.now())
                .appointmentEnd(OffsetDateTime.now())
                .notes("http://url")
                .countsTowardsRarDays(true)
                .build()))
            .post("offenders/crn/X320741/sentence/2500295345/appointments/context/commissioned-rehabilitation-services")
            .then()
            .assertThat()
            .statusCode(HttpStatus.CREATED.value())
            .body("appointmentId", equalTo(2500029015L))
            .body("type", equalTo("CRSAPT"))
            .body("typeDescription", equalTo("Appointment with CRS Provider (NS)"))
            .body("appointmentStart", equalTo("2021-03-01T13:01:02Z"))
            .body("appointmentEnd", equalTo("2021-03-01T14:03:04Z"));
    }

    @Test
    public void shouldReturnOKAfterPatchingUpdatingAppointmentUsingContextlessClientEndpoint() {

        deliusApiMockServer.stubPatchContactToDeliusApi(2500029015L);

        final var token = createJwt("bob", Collections.singletonList("ROLE_COMMUNITY_INTERVENTIONS_UPDATE"));

        given()
            .when()
            .auth().oauth2(token)
            .contentType(String.valueOf(ContentType.APPLICATION_JSON))
            .body(ContextlessAppointmentOutcomeRequest.builder()
                .notes("some notes")
                .attended("LATE")
                .notifyPPOfAttendanceBehaviour(true)
                .build())
            .post("offenders/crn/X320741/appointments/2500029015/outcome/context/commissioned-rehabilitation-services")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("appointmentId", equalTo(2500029015L));
    }

    @Test
    public void shouldReturnOKAfterReschedulingAppointmentUsingContextlessClientEndpoint() {

        deliusApiMockServer.stubReplaceContactToDeliusApi();

        final var token = createJwt("bob", Collections.singletonList("ROLE_COMMUNITY_INTERVENTIONS_UPDATE"));

        given()
            .when()
            .auth().oauth2(token)
            .contentType(String.valueOf(ContentType.APPLICATION_JSON))
            .body(ContextlessAppointmentRescheduleRequest.builder()
                .updatedAppointmentStart(OffsetDateTime.of(2025, 9, 2, 11, 0, 0, 0, ZoneOffset.UTC))
                .updatedAppointmentEnd(OffsetDateTime.of(2025, 9, 2, 12, 0, 0, 0, ZoneOffset.UTC))
                .initiatedByServiceProvider(true)
                .build())
            .post("offenders/crn/X320741/appointments/2512709905/reschedule/context/commissioned-rehabilitation-services")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("appointmentId", equalTo(2500029016L));
    }

    @Test
    public void shouldReturnOKAfterRelocatingAppointment() {

        deliusApiMockServer.stubPatchContactToDeliusApi(2512709905L);

        final var token = createJwt("bob", Collections.singletonList("ROLE_COMMUNITY_INTERVENTIONS_UPDATE"));

        given()
            .when()
            .auth().oauth2(token)
            .contentType(String.valueOf(ContentType.APPLICATION_JSON))
            .body(AppointmentRelocateRequest.builder()
                .officeLocationCode("CRSLOND")
                .build())
            .post("offenders/crn/X320741/appointments/2512709905/relocate")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("appointmentId", equalTo(2512709905L));
    }

    private String createJwt(final String user, final List<String> roles) {
        return jwtAuthenticationHelper.createJwt(JwtParameters.builder()
                .username(user)
                .roles(roles)
                .scope(Arrays.asList("read", "write"))
                .expiryTime(Duration.ofDays(1))
                .build());
    }

    private AppointmentCreateRequest anAppointmentCreateRequest(String type) {
        return AppointmentCreateRequest.builder()
            .requirementId(12345678L)
            .contactType(type)
            .appointmentStart(OffsetDateTime.now())
            .appointmentEnd(OffsetDateTime.now())
            .officeLocationCode("CRSSHEF")
            .notes("http://url")
            .providerCode("CRS")
            .staffCode("CRSUATU")
            .teamCode("CRSUAT")
            .sensitive(true)
            .build();
    }
}
