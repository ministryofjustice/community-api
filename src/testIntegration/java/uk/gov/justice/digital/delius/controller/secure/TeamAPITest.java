package uk.gov.justice.digital.delius.controller.secure;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.digital.delius.FlywayRestoreExtension;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

@ExtendWith({SpringExtension.class, FlywayRestoreExtension.class})
public class TeamAPITest extends IntegrationTestBase {
    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    public void gettingAllStaffForTeam() {
        final var token = createJwt("ROLE_COMMUNITY");

        final var staff = withArgs("C00P002");

        given()
            .auth().oauth2(token)
            .when()
            .get("/teams/C00T01/staff")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("size()", greaterThan(0))
            .rootPath("find { it.staffCode == '%s' }")

            .body("staffCode", staff, Matchers.equalTo("C00P002"))
            .body("staff.forenames", staff, Matchers.equalTo("Nolan ZZ"))
            .body("staff.surname", staff, Matchers.equalTo("Murders"))
            .body("staffGrade.code", staff, Matchers.equalTo("CRCM"))
            .body("staffGrade.description", staff, Matchers.equalTo("CRC - PO"))
            .body("staffIdentifier", staff, Matchers.equalTo(2500000012L));
    }

    @Test
    public void attemptingToGetAllStaffForInactiveTeam() {
        final var token = createJwt("ROLE_COMMUNITY");

        given()
            .auth().oauth2(token)
            .when()
            .get("/teams/C19T01/staff")
            .then()
            .assertThat()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void attemptingToAllStaffForMissingTeam() {
        final var token = createJwt("ROLE_COMMUNITY");

        given()
            .auth().oauth2(token)
            .when()
            .get("/teams/some-missing-team/staff")
            .then()
            .assertThat()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void attemptingToGetAllStaffForTeamWithNoStaff() {
        final var token = createJwt("ROLE_COMMUNITY");

        given()
            .auth().oauth2(token)
            .when()
            .get("/teams/CRSUAT/staff")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("size()", equalTo(0));
    }

    @Test
    public void attemptingAllStaffForWithoutRequiredRole() {
        final var token = createJwt("SOME_OTHER_ROLE");

        given()
            .auth().oauth2(token)
            .when()
            .get("/teams/any-team-code/staff")
            .then()
            .assertThat()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

}
