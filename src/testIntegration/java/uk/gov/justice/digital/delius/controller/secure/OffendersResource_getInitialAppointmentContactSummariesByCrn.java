package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.justice.digital.delius.FlywayRestoreExtension;
import uk.gov.justice.digital.delius.data.api.ContactSummary;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(FlywayRestoreExtension.class)
public class OffendersResource_getInitialAppointmentContactSummariesByCrn extends IntegrationTestBase {

    @Test
    void getsInitialAppointmentsByCrn() {
        ContactSummary[] appointments = given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/offenders/crn/X320741/contact-summary/inductions")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(ContactSummary[].class);
        assertThat(appointments.length).isEqualTo(2);
        assertThat(appointments[0].getType().getCode()).isEqualTo("COAI");
    }

    @Test
    void getInitialAppointmentsFromDateByCrn() {
        ContactSummary[] appointments = given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/offenders/crn/X320741/contact-summary/inductions?contactDateFrom=2021-09-04")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(ContactSummary[].class);
        assertThat(appointments.length).isEqualTo(1);

    }
}
