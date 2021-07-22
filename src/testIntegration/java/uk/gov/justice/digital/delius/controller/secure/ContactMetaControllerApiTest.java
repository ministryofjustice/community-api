package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.Matchers.equalTo;


public class ContactMetaControllerApiTest extends IntegrationTestBase {

    @Test
    public void getAllContactTypes(){
        final var token = createJwt("ROLE_COMMUNITY");

        final var officeVisit = withArgs("COAP");

        given()
            .auth().oauth2(token)
            .when()
            .get("/contact-types")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("size()", equalTo(929))
            .root("find { it.code == '%s' }")
            .body("description", officeVisit, equalTo("Planned Office Visit (NS)"))
            .body("appointment", officeVisit, equalTo(true));
    }

    @Test
    public void getContactTypesForACategory(){
        final var token = createJwt("ROLE_COMMUNITY");

        final var phoneContact = withArgs("CTOB");
        final var emailContact = withArgs("CMOB");

        given()
            .auth().oauth2(token)
            .when()
            .queryParam("categories", "LT")
            .get("/contact-types")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("size()", equalTo(43))
            .root("find { it.contactType == '%s' }")
            .body("description", phoneContact, equalTo("Phone Contact to Offender"))
            .body("appointment", phoneContact, equalTo(false))
            .body("description", emailContact, equalTo("eMail/Text to Offender"))
            .body("appointment", emailContact, equalTo(false));


    }
}
