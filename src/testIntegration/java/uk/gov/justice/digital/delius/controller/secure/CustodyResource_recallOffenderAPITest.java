package uk.gov.justice.digital.delius.controller.secure;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import uk.gov.justice.digital.delius.FlywayRestoreExtension;
import uk.gov.justice.digital.delius.controller.wiremock.DeliusApiExtension;
import uk.gov.justice.digital.delius.controller.wiremock.DeliusApiMockServer;
import uk.gov.justice.digital.delius.data.api.Custody;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(FlywayRestoreExtension.class)
public class CustodyResource_recallOffenderAPITest extends IntegrationTestBase {

    private static final DeliusApiMockServer deliusApiMockServer = new DeliusApiMockServer(7999);

    @RegisterExtension
    static DeliusApiExtension deliusExtension = new DeliusApiExtension(deliusApiMockServer);

    public static final String BODY = "{\"nomsPrisonInstitutionCode\" : \"MDI\", \"recallDate\" : \"2020-12-22\", \"reason\" : \"ADMISSION\", \"probableCause\" : \"RECALL\" }";

    @SpyBean
    private TelemetryClient telemetryClient;

    @Test
    @DisplayName("Will return 403 without the correct role")
    public void mustHaveUpdateRole() {
        final var token = tokenWithRoleCommunity();

        given()
            .auth().oauth2(token)
            .contentType("application/json")
            .body(BODY)
            .when()
            .put("/offenders/nomsNumber/G0560UO/recalled")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("Will return 409 for an offender with no active convictions")
    public void offenderWithNoActiveEvents() {
        final var token = tokenWithRoleCommunityAndCustodyUpdate();

        given()
            .auth().oauth2(token)
            .contentType("application/json")
            .body(BODY)
            .when()
            .put("/offenders/nomsNumber/g4106un/recalled")
            .then()
            .assertThat()
            .statusCode(HttpStatus.CONFLICT.value())
            .body("developerMessage", equalTo("Expected offender 12 to have a single custody related event but found 0 events"));

        verify(telemetryClient).trackEvent(eq("P2POffenderRecalledNoSingleConviction"), any(), isNull());
    }

    @Test
    @DisplayName("Will return 404 for a offender that is not found")
    public void missingOffender() {
        final var token = tokenWithRoleCommunityAndCustodyUpdate();

        given()
            .auth().oauth2(token)
            .contentType("application/json")
            .body(BODY)
            .when()
            .put("/offenders/nomsNumber/X1235YZ/recalled")
            .then()
            .assertThat()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .body("developerMessage", equalTo("Offender with nomsNumber X1235YZ not found"));

        verifyNoInteractions(telemetryClient);
    }

    @Test
    @DisplayName("Will also return 404 for a offender that has multiple nomis records with multiple active sentences")
    public void duplicateOffender() {
        final var token = tokenWithRoleCommunityAndCustodyUpdate();

        given()
            .auth().oauth2(token)
            .contentType("application/json")
            .body(BODY)
            .when()
            .put("/offenders/nomsNumber/G3636DD/recalled")
            .then()
            .assertThat()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .body("developerMessage", equalTo("Expecting only a single active offender but found 2 offenders with noms number G3636DD"));

        verify(telemetryClient).trackEvent(eq("P2POffenderRecalledMultipleOffendersFound"), any(), isNull());

    }

    @Test
    @DisplayName("Will succeed for an offender that has multiple nomis records where one is on an active sentence and the other is not")
    public void duplicateOffenderSingleActive() {
        deliusApiMockServer.stubPostRecallDuplicateOffenderSingleActiveDeliusApi();
        final var token = tokenWithRoleCommunityAndCustodyUpdate();

        final var custody = given()
            .auth().oauth2(token)
            .contentType("application/json")
            .body(BODY)
            .when()
            .put("/offenders/nomsNumber/G3232DD/recalled")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .body()
            .as(Custody.class);

        assertThat(custody.getInstitution().getNomsPrisonInstitutionCode()).isEqualTo("BWI");
        verify(telemetryClient).trackEvent(eq("P2POffenderRecalled"), any(), isNull());
    }

    @Test
    @DisplayName("Successfully update the recalled offender")
    public void processesOffenderRecalled() {
        deliusApiMockServer.stubPostRecallDeliusApi();
        final var token = tokenWithRoleCommunityAndCustodyUpdate();

        final var custody = given()
            .auth().oauth2(token)
            .contentType("application/json")
            .body(BODY)
            .when()
            .put("/offenders/nomsNumber/G9542VP/recalled")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .body()
            .as(Custody.class);

        assertThat(custody.getInstitution().getNomsPrisonInstitutionCode()).isEqualTo("BWI");
        verify(telemetryClient).trackEvent(eq("P2POffenderRecalled"), any(), isNull());
    }
}
