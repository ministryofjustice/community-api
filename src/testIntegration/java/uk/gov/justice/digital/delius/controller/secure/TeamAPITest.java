package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.digital.delius.FlywayRestoreExtension;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
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
        ;

        // AND I count the number of POM Teams
        final var countOfPOMTeams = countOf("SELECT COUNT(*) as count from TEAM where DESCRIPTION = 'Prison Offender Managers'");

        // THEN there should be a team for each prison except for the OUT "prison"
        assertThat(countOfPOMTeams).isEqualTo(countOfInstitutions - 1);


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

    private long countOf(String s) {
        return (Long) jdbcTemplate.query(
                s,
                new ColumnMapRowMapper()).get(0).get("count");
    }

}
