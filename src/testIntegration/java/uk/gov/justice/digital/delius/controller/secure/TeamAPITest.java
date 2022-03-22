package uk.gov.justice.digital.delius.controller.secure;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.digital.delius.FlywayRestoreExtension;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.withArgs;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith({SpringExtension.class, FlywayRestoreExtension.class})
@DisplayName("POST /teams/prisonOffenderManagers/create")
public class TeamAPITest extends IntegrationTestBase {
    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    @DisplayName("must have `ROLE_COMMUNITY_CUSTODY_UPDATE` to access this service")
    public void mustHaveCommunityRole() {
        final var token = createJwt("ROLE_COMMUNITY");

        given()
                .auth().oauth2(token)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .post("/teams/prisonOffenderManagers/create")
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Will create missing Prison Offender Manager teams")
    public void canGetNextUpdateWithDateChanged() {
        final var EXPECTED_NUMBER_TEAMS = 129;
        final var EXPECTED_NUMBER_INSTITUTIONS = 130;
        // GIVEN there are 130 valid institutions identified has NOMIS prisons
        final var countOfInstitutions = countOf("SELECT COUNT(*) as count from R_INSTITUTION i JOIN PROBATION_AREA pa ON (i.INSTITUTION_ID = pa.INSTITUTION_ID) where i.NOMIS_CDE_CODE IS NOT NULL");
        assertThat(countOfInstitutions).isEqualTo(EXPECTED_NUMBER_INSTITUTIONS);

        // WHEN I request all missing teams to be created
        given()
                .auth().oauth2(createJwt("ROLE_COMMUNITY_CUSTODY_UPDATE"))
                .when()
                .post("/teams/prisonOffenderManagers/create")
                .then()
                .statusCode(200)
                .body("teams.size()", equalTo(EXPECTED_NUMBER_TEAMS))
                .body("teams.size()", equalTo((int)countOfInstitutions - 1))
                .body("teams[0].description", equalTo("Prison Offender Managers"))
                .body("teams[0].code", endsWith("POM"))
                .body("teams[0].localDeliveryUnit.code", endsWith("POM"))
                .body("teams[0].borough.code", endsWith("POM"))
                .body("teams[0].teamType.code", endsWith("POM"))
                .body("unallocatedStaff.size()", equalTo(EXPECTED_NUMBER_TEAMS))
                .body("unallocatedStaff.size()", equalTo((int)countOfInstitutions - 1))
                .body("unallocatedStaff[0].forenames", equalTo("Unallocated"))
                .body("unallocatedStaff[0].surname", equalTo("Staff"))
                .body("unallocatedStaff[0].code", endsWith("POMU"))
                .body("unallocatedStaff[0].unallocated", equalTo(true))
        ;

        // AND I count the number of POM Teams
        final var countOfPOMTeams = countOf("SELECT COUNT(*) as count from TEAM where DESCRIPTION = 'Prison Offender Managers'");

        // THEN there should be a team for each prison except for the OUT "prison"
        assertThat(countOfPOMTeams).isEqualTo(countOfInstitutions - 1);

        // AND I count the number of Unallocated staff POM members
        final var countOfUnallocatedPOMStaff = countOf("SELECT COUNT(*) as count from TEAM t join STAFF_TEAM st ON st.TEAM_ID = t.TEAM_ID join STAFF s ON s.STAFF_ID = st.STAFF_ID  where t.DESCRIPTION = 'Prison Offender Managers' and s.OFFICER_CODE like '%U'");

        // THEN there should be a unallocated team member for each prison except for the OUT "prison"
        assertThat(countOfUnallocatedPOMStaff).isEqualTo(countOfInstitutions - 1);


        // AND if I make a second request
        given()
                .auth().oauth2(createJwt("ROLE_COMMUNITY_CUSTODY_UPDATE"))
                .when()
                .post("/teams/prisonOffenderManagers/create")
                .then()
                .statusCode(200)
                .body("teams.size()", is(0)); // THEN nothing is created

        // THEN the number teams stays the same
        assertThat(countOf("SELECT COUNT(*) as count from TEAM where DESCRIPTION = 'Prison Offender Managers'")).isEqualTo(countOfInstitutions - 1);

    }

    @Test
    public void gettingTeamOfficeLocations() {
        final var token = createJwt("ROLE_COMMUNITY");

        final var lincoln = withArgs("LNS_LNC");
        final var grantham = withArgs("LNS_GTM");

        given()
            .auth().oauth2(token)
            .when()
            .get("/teams/C04000/office-locations")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("size()", greaterThan(0))
            .root("find { it.code == '%s' }")

            .body("description", lincoln, Matchers.equalTo("Lincoln office"))
            .body("buildingName", lincoln, Matchers.nullValue())
            .body("buildingNumber", lincoln, Matchers.equalTo("8"))
            .body("streetName", lincoln, Matchers.equalTo("Corporation Street"))
            .body("townCity", lincoln, Matchers.equalTo("Lincoln"))
            .body("county", lincoln, Matchers.equalTo("Lincolnshire"))
            .body("postcode", lincoln, Matchers.equalTo("LN2 1HN"))

            .body("description", grantham, Matchers.equalTo("Grantham office"))
            .body("buildingName", grantham, Matchers.equalTo("Grange House"))
            .body("buildingNumber", grantham, Matchers.equalTo("46"))
            .body("streetName", grantham, Matchers.equalTo("Union Street"))
            .body("townCity", grantham, Matchers.equalTo("Grantham"))
            .body("county", grantham, Matchers.equalTo("Lincolnshire"))
            .body("postcode", grantham, Matchers.equalTo("NG31 6NZ"));
    }

    @Test
    public void attemptingToGetAllTeamOfficeLocationsForInactiveTeam() {
        final var token = createJwt("ROLE_COMMUNITY");

        given()
            .auth().oauth2(token)
            .when()
            .get("/teams/C19T01/office-locations")
            .then()
            .assertThat()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void attemptingToGetAllTeamOfficeLocationsForMissingTeam() {
        final var token = createJwt("ROLE_COMMUNITY");

        given()
            .auth().oauth2(token)
            .when()
            .get("/teams/some-missing-team/office-locations")
            .then()
            .assertThat()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void attemptingToGetAllTeamOfficeLocationsForTeamWithNoOfficeLocations() {
        final var token = createJwt("ROLE_COMMUNITY");

        given()
            .auth().oauth2(token)
            .when()
            .get("/teams/SFIST1/office-locations")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("size()", equalTo(0));
    }

    @Test
    public void attemptingToGetAllTeamOfficeLocationsWithoutRequiredRole() {
        final var token = createJwt("SOME_OTHER_ROLE");

        given()
            .auth().oauth2(token)
            .when()
            .get("/teams/any-team-code/office-locations")
            .then()
            .assertThat()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    private long countOf(String s) {
        return (Long) jdbcTemplate.query(
                s,
                new ColumnMapRowMapper()).get(0).get("count");
    }


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
            .root("find { it.code == '%s' }")

            .body("code", staff, Matchers.equalTo("C00P002"))
            .body("forenames", staff, Matchers.equalTo("Nolan ZZ"))
            .body("surname", staff, Matchers.equalTo("Murders"))
            .body("staffGrade", staff, Matchers.equalTo("CRCM"));
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
