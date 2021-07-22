package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;


public class ContactMetaControllerApiTest extends IntegrationTestBase {

    @Test
    public void getAllContactTypes(){
        final var token = createJwt("ROLE_COMMUNITY");

        given()
            .auth().oauth2(token)
            .when()
            .get("/contact-types")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("size()", greaterThan(0));
//            .root("find { it.contactType == '%s' }")
//            .body("description", alcohol, equalTo("Citizenship Alcohol Session (NS)"))
//            .body("requiresLocation", alcohol, equalTo("REQUIRED"))
//            .body("orderTypes", alcohol, equalTo(List.of("CJA")))
//            .body("description", homeVisit, equalTo("Home Visit to Case (NS)"))
//            .body("requiresLocation", homeVisit, equalTo("NOT_REQUIRED"))
//            .body("orderTypes", homeVisit, equalTo(List.of("CJA", "LEGACY")))
//            .body("description", other, equalTo("Other Appointment (Non NS)"))
//            .body("requiresLocation", other, equalTo("OPTIONAL"))
//            .body("orderTypes", other, equalTo(List.of("CJA", "LEGACY")))
//            .body("orderTypes", polygraph, equalTo(List.of()));


    }

    @Test
    public void getContactTypesForACategory(){
        final var token = createJwt("ROLE_COMMUNITY");

        given()
            .auth().oauth2(token)
            .when()
            .queryParam("categories", "LT")
            .get("/contact-types")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("size()", greaterThan(0));
//            .root("find { it.contactType == '%s' }")
//            .body("description", alcohol, equalTo("Citizenship Alcohol Session (NS)"))
//            .body("requiresLocation", alcohol, equalTo("REQUIRED"))
//            .body("orderTypes", alcohol, equalTo(List.of("CJA")))
//            .body("description", homeVisit, equalTo("Home Visit to Case (NS)"))
//            .body("requiresLocation", homeVisit, equalTo("NOT_REQUIRED"))
//            .body("orderTypes", homeVisit, equalTo(List.of("CJA", "LEGACY")))
//            .body("description", other, equalTo("Other Appointment (Non NS)"))
//            .body("requiresLocation", other, equalTo("OPTIONAL"))
//            .body("orderTypes", other, equalTo(List.of("CJA", "LEGACY")))
//            .body("orderTypes", polygraph, equalTo(List.of()));


    }
}
