package uk.gov.justice.digital.delius.controller.secure;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.StaffDetails;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class TeamResource_AllStaffAPITest extends IntegrationTestBase {
    @Test
    public void getStaffByTeamCode() {
        var staffDetails = given()
            .auth().oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("teams/C01T04/staff")
            .then()
            .extract().body().as(StaffDetails[].class);

        var staffWithEmail = Arrays.stream(staffDetails).filter(staff -> "SheilaHancockNPS".equals(staff.getUsername())).findFirst();
        Assertions.assertThat(staffWithEmail.get().getEmail()).isEqualTo("sheila.hancock@justice.gov.uk");
    }

}
