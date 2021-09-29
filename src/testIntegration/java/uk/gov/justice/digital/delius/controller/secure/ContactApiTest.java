package uk.gov.justice.digital.delius.controller.secure;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@ExtendWith(SpringExtension.class)
public class ContactApiTest extends IntegrationTestBase {

    @Test
    @DisplayName("will return 403 forbidden")
    public void attemptingToContactsWithoutCorrectRole() {
        final var token = createJwt("SOME_OTHER_ROLE");
        given()
            .auth().oauth2(token)
            .when()
            .get("/offenders/crn/X1234/contacts/20")
            .then()
            .assertThat()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("will return a contact summary")
    public void getAContact() {
        final var token = createJwt("ROLE_COMMUNITY");
        final var response = given()
            .auth().oauth2(token)
            .when()
            .get("/offenders/crn/X320741/contacts/2502719240")
            .then()
            .assertThat()
            .statusCode((OK.value()))
            .body("contactId", equalTo(2502719240L))
            .body("staff.code", equalTo("N02SP5U"))
            .body("staff.forenames", equalTo("Unallocated"))
            .body("staff.surname", equalTo("Staff"))
            .body("staff.unallocated", equalTo(true))
            .body("notes", equalTo("The notes field"))
            .body("lastUpdatedDateTime", equalTo("2019-09-13T00:00:00+01:00"))
            .body("lastUpdatedByUser.surname", equalTo("Marke"))
            .body("lastUpdatedByUser.forenames", equalTo("Andy"));
    }

    @Test
    @DisplayName("will return a rar contact summary")
    public void getARarContact() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320741/contacts/2503537768")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("contactId", equalTo(2503537768L))
            .body("contactStart", equalTo("2017-12-02T12:00:00Z"))
            .body("contactEnd", equalTo("2017-12-02T13:00:00Z"))
            .body("type.code", equalTo("CHVS"))
            .body("type.description", equalTo("Home Visit to Case (NS)"))
            .body("type.appointment", equalTo(true))
            .body("type.nationalStandard", equalTo(true))
            .body("type.systemGenerated", equalTo(false))
            .body("provider.code", equalTo("N02"))
            .body("provider.description", equalTo("NPS North East"))
            .body("team.code", equalTo("N02UAT"))
            .body("team.description", equalTo("Unallocated Team(N02)"))
            .body("staff.code", equalTo("N02UATU"))
            .body("staff.forenames", equalTo("Unallocated Staff(N02)"))
            .body("staff.surname", equalTo("Staff"))
            .body("staff.unallocated", equalTo(true))
            .body("rarActivity", equalTo(true))
            .body("notes", equalTo("Some RAR notes"))
            .body("rarActivityDetail.requirementId", equalTo(2500083653L))
            .body("rarActivityDetail.nsiId", equalTo(2500018999L))
            .body("rarActivityDetail.type.code", equalTo("APCUS"))
            .body("rarActivityDetail.type.description", equalTo("Custody - Accredited Programme"))
            .body("rarActivityDetail.subtype.code", equalTo("APHSP"))
            .body("rarActivityDetail.subtype.description", equalTo("Healthy Sex Programme (HCP)"))
            .body("lastUpdatedDateTime", equalTo("2019-09-04T00:00:00+01:00"))
            .body("lastUpdatedByUser.forenames", equalTo("Andy"))
            .body("lastUpdatedByUser.surname", equalTo("Marke"));
    }

    @Test
    @DisplayName("will return 404 not found when contact does not exist")
    public void contactNotFound() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320741/contacts/123")
            .then()
            .assertThat()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("will return 404 not found when offender does not exist")
    public void offenderNotFound() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X000000/contacts/2503537768")
            .then()
            .assertThat()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("will return 404 not found when contact exists but is not associated to offender")
    public void contactNotOffenders() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320811/contacts/2503537768")
            .then()
            .assertThat()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
