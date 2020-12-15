package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.AccessLimitation;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffendersResource_checkUserAccessByCrn extends IntegrationTestBase {

    @Test
    public void canCheckUserAccessByCrnNoRestrictionsOrExclusions() {
        final var accessLimitation = given()
                .auth()
                .oauth2(createJwtWithUsername("bob.jones", "ROLE_COMMUNITY"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X320741/userAccess")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(AccessLimitation.class);

        assertThat(accessLimitation.isUserExcluded()).isFalse();
        assertThat(accessLimitation.getExclusionMessage()).isNullOrEmpty();
        assertThat(accessLimitation.isUserRestricted()).isFalse();
        assertThat(accessLimitation.getRestrictionMessage()).isNullOrEmpty();
    }

    @Test
    public void canCheckUserAccessByCrnUserExcluded() {
        final var accessLimitation = given()
                .auth()
                .oauth2(createJwtWithUsername("bob.jones", "ROLE_COMMUNITY"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X440877/userAccess")
                .then()
                .statusCode(403)
                .extract()
                .body()
                .as(AccessLimitation.class);

        assertThat(accessLimitation.isUserExcluded()).isTrue();
        assertThat(accessLimitation.getExclusionMessage()).isEqualTo("You are excluded from viewing this offender record. Please contact a system administrator");
        assertThat(accessLimitation.isUserRestricted()).isFalse();
        assertThat(accessLimitation.getRestrictionMessage()).isNullOrEmpty();
    }

    @Test
    public void canCheckUserAccessByCrnNotRestrictedUserForOffenderSoCannotSeeDetails() {
        final var accessLimitation = given()
                .auth()
                .oauth2(createJwtWithUsername("bob.jones", "ROLE_COMMUNITY"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X440890/userAccess")
                .then()
                .statusCode(403)
                .extract()
                .body()
                .as(AccessLimitation.class);

        assertThat(accessLimitation.isUserExcluded()).isFalse();
        assertThat(accessLimitation.getExclusionMessage()).isNullOrEmpty();
        assertThat(accessLimitation.isUserRestricted()).isTrue();
        assertThat(accessLimitation.getRestrictionMessage()).isEqualTo("This is a restricted offender record. Please contact a system administrator");
    }

    @Test
    public void canCheckUserAccessByCrnRestrictedUserForOffenderSoCanSeeDetails() {
        final var accessLimitation = given()
                .auth()
                .oauth2(createJwtWithUsername("bobby.davro", "ROLE_COMMUNITY"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X440890/userAccess")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(AccessLimitation.class);

        assertThat(accessLimitation.isUserExcluded()).isFalse();
        assertThat(accessLimitation.getExclusionMessage()).isNullOrEmpty();
        assertThat(accessLimitation.isUserRestricted()).isFalse();
        assertThat(accessLimitation.getRestrictionMessage()).isNullOrEmpty();
    }

    @Test
    public void getOffenderDetailsByCrn_offenderNotFound_returnsNotFound() {
        given()
        .auth()
              .oauth2(createJwtWithUsername("bob.jones", "ROLE_COMMUNITY"))
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .get("/offenders/crn/X777777/userAccess")
        .then()
        .statusCode(404);
    }

    @Test
    public void canCheckUserAccessByCrnInvalidUser() {
        final var responseText = given()
                .auth()
                .oauth2(createJwtWithUsername("bob", "ROLE_COMMUNITY"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X440877/userAccess")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .asString();

        assertThat(responseText).isEqualTo("Can't resolve user: bob");
}}
