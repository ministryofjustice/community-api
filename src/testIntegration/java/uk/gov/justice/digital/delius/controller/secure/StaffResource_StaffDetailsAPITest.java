package uk.gov.justice.digital.delius.controller.secure;

import lombok.val;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.ProbationArea;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class StaffResource_StaffDetailsAPITest extends IntegrationTestBase {
    @Test
    public void canRetrieveStaffDetailsByStaffIdentifier() {

        val staffDetails = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("staff/staffIdentifier/11")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(StaffDetails.class);

        assertThat(staffDetails).isNotNull();
    }

    @Test
    public void retrievingStaffDetailsByStaffIdentifierReturn404WhenStaffDoesNotExist() {

        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("staff/staffIdentifier/99999")
                .then()
                .statusCode(404);
    }

    @Test
    public void canRetrieveStaffDetailsByUsername() {

        val staffDetails = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("staff/username/SheilaHancockNPS")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(StaffDetails.class);

        assertThat(staffDetails).isNotNull();

        assertThat(staffDetails.getProbationArea()).isEqualTo((ProbationArea.builder()
            .probationAreaId(11L)
            .code("ESX")
            .description("Essex")
            .organisation(KeyValue.builder().code("EA").description("Eastern").build())
            .build()));
    }

    @Test
    public void canRetrieveStaffDetailsByUsernameIgnoresCase() {

        val staffDetails = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("staff/username/sheilahancocknps")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(StaffDetails.class);

        assertThat(staffDetails).isNotNull();
    }

    @Test
    public void retrievingStaffDetailsByUsernameReturn404WhenUserExistsButStaffDoesNot() {

        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("staff/username/NoStaffUserNPS")
                .then()
                .statusCode(404);
    }

    @Test
    public void canRetrieveStaffDetailsByStaffCode() {
        val staffDetails = given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("staff/staffCode/SH00001")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(StaffDetails.class);

        assertThat(staffDetails).isNotNull();

        assertThat(staffDetails.getProbationArea()).isEqualTo((ProbationArea.builder()
            .probationAreaId(11L)
            .code("ESX")
            .description("Essex")
            .organisation(KeyValue.builder().code("EA").description("Eastern").build())
            .build()));
    }

    @Test
    public void retrievingStaffDetailsByStaffCodeReturn404WhenUserExistsButStaffDoesNot() {

        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("staff/staffCode/noCode123")
            .then()
            .statusCode(404);
    }

    @Test
    public void retrieveStaffDetailsForMultipleUsers() {

        val staffDetails = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .body(getUsernames(Set.of("sheilahancocknps", "JimSnowLdap")))
                .when()
                .post("staff/list")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(StaffDetails[].class);

        var jimSnowUserDetails = Arrays.stream(staffDetails).filter(s -> s.getUsername().equals("JimSnowLdap")).findFirst().get();
        var sheilaHancockUserDetails = Arrays.stream(staffDetails).filter(s -> s.getUsername().equals("SheilaHancockNPS")).findFirst().get();

        assertThat(staffDetails.length).isEqualTo(2);

        assertThat(jimSnowUserDetails.getEmail()).isEqualTo("jim.snow@justice.gov.uk");
        assertThat(jimSnowUserDetails.getStaff().getForenames()).isEqualTo("JIM");
        assertThat(jimSnowUserDetails.getStaff().getSurname()).isEqualTo("SNOW");
        assertThat(jimSnowUserDetails.getTeams()).isNull();

        assertThat(sheilaHancockUserDetails.getEmail()).isEqualTo("sheila.hancock@justice.gov.uk");
        assertThat(sheilaHancockUserDetails.getStaff().getForenames()).isEqualTo("SHEILA LINDA");
        assertThat(sheilaHancockUserDetails.getStaff().getSurname()).isEqualTo("HANCOCK");
        assertThat(sheilaHancockUserDetails.getTeams().stream().findFirst().get().getEmailAddress()).isEqualTo("Sheila.HancockNPS@moj.gov.uk");
        assertThat(sheilaHancockUserDetails.getTeams().stream().findFirst().get().getStartDate()).isEqualTo(LocalDate.of(2014, 8,29));
        assertThat(sheilaHancockUserDetails.getTeams().stream().findFirst().get().getEndDate()).isEqualTo(LocalDate.of(2025, 6, 30));
    }

    @Test
    public void retrieveDetailsWhenUsersDoNotExist() {

        val staffDetails = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .body(getUsernames(Set.of("xxxppp1ps", "dddiiiyyyLdap")))
                .when()
                .post("staff/list")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(StaffDetails[].class);

        assertThat(staffDetails).isEmpty();
    }

    @Test
    public void retrieveMultipleUserDetailsWithNoBodyContentReturn400() {

        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .post("staff/list")
                .then()
                .statusCode(400);
    }

    @Test
    public void retrieveStaffDetailsForMultipleStaffCodes() {

        val staffDetails = given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .body(getUsernames(Set.of("SH00001")))
            .when()
            .post("staff/list/staffCodes")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(StaffDetails[].class);

        var sheilaHancockUserDetails = Arrays.stream(staffDetails).filter(s -> s.getUsername().equals("SheilaHancockNPS")).findFirst().get();

        assertThat(staffDetails.length).isEqualTo(1);

        assertThat(sheilaHancockUserDetails.getEmail()).isEqualTo("sheila.hancock@justice.gov.uk");
        assertThat(sheilaHancockUserDetails.getStaff().getForenames()).isEqualTo("SHEILA LINDA");
        assertThat(sheilaHancockUserDetails.getStaff().getSurname()).isEqualTo("HANCOCK");
        assertThat(sheilaHancockUserDetails.getTeams().stream().findFirst().get().getEmailAddress()).isEqualTo("Sheila.HancockNPS@moj.gov.uk");
        assertThat(sheilaHancockUserDetails.getTeams().stream().findFirst().get().getStartDate()).isEqualTo(LocalDate.of(2014, 8,29));
        assertThat(sheilaHancockUserDetails.getTeams().stream().findFirst().get().getEndDate()).isEqualTo(LocalDate.of(2025, 6, 30));
    }

    @Test
    public void retrieveDetailsWhenStaffCodesDoNotExist() {

        val staffDetails = given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .body(getUsernames(Set.of("xxxppp1ps", "dddiiiyyyLdap")))
            .when()
            .post("staff/list/staffCodes")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(StaffDetails[].class);

        assertThat(staffDetails).isEmpty();
    }

    @Test
    public void retrieveMultipleUserDetailsByStaffCodesWithNoBodyContentReturn400() {

        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .post("staff/list/staffCodes")
            .then()
            .statusCode(400);
    }


    private String getUsernames(Set <String> usernames) {
        return writeValueAsString(usernames);
    }



    @Test
    public void retrieveProbationAreaHeadsWherePDUDoesNotExist() {

        val staffDetails = given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .body(getUsernames(Set.of("xxxppp1ps", "dddiiiyyyLdap")))
            .when()
            .get("/staff/pduHeads/N07123")
            .then()
            .statusCode(404);
    }


    @Test
    public void retrieveProbationAreaHeadsHasValues() {
        val staffDetails = given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/staff/pduHeads/N01ALL")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(Staff[].class);

        assertThat(staffDetails).hasSize(1);
    }


    @Test
    public void retrieveProbationAreaHeadsHasNoValues() {
        val staffDetails = given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/staff/pduHeads/N02ALL")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(Staff[].class);

        assertThat(staffDetails).isEmpty();
    }

    @Test
    public void ignoresPdusThatAreLinkedToInactiveProbationAreas() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/staff/pduHeads/WPTNWS")
            .then()
            .statusCode(200);
    }
}
