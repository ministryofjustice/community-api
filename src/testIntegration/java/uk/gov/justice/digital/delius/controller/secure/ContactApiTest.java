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
            .body("notes", equalTo("The notes field"));
    }
}
