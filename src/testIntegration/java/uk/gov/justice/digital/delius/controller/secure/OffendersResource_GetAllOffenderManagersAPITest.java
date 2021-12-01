package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;

import java.time.LocalDate;
import java.time.Month;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class OffendersResource_GetAllOffenderManagersAPITest extends IntegrationTestBase {

    @Nested
    class allOffenderManagersByNomsNumber {

        @Test
        void canGetAllOffenderManagersByNOMSNumber() {
            final var offenderManagers = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/G9542VP/allOffenderManagers")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager[].class);

            assertThat(offenderManagers).hasSize(2);

            final var communityOffenderManager = Stream.of(offenderManagers).filter(not(CommunityOrPrisonOffenderManager::getIsPrisonOffenderManager))
                .findAny().orElseThrow();
            final var prisonOffenderManager = Stream.of(offenderManagers).filter(CommunityOrPrisonOffenderManager::getIsPrisonOffenderManager)
                .findAny().orElseThrow();

            assertThat(communityOffenderManager.getIsResponsibleOfficer()).isFalse();
            assertThat(communityOffenderManager.getIsUnallocated()).isTrue();
            assertThat(communityOffenderManager.getProbationArea()).isNotNull();
            assertThat(communityOffenderManager.getProbationArea().getInstitution()).isNull();
            assertThat(communityOffenderManager.getStaff()).isNotNull();
            assertThat(communityOffenderManager.getTeam()).isNotNull();
            assertThat(communityOffenderManager.getStaffCode()).isEqualTo("N02AAMU");
            assertThat(communityOffenderManager.getGrade()).isNull();


            assertThat(prisonOffenderManager.getIsResponsibleOfficer()).isTrue();
            assertThat(prisonOffenderManager.getIsUnallocated()).isFalse();
            assertThat(prisonOffenderManager.getProbationArea()).isNotNull();
            assertThat(prisonOffenderManager.getProbationArea().getInstitution()).isNotNull();
            assertThat(prisonOffenderManager.getStaff()).isNotNull();
            assertThat(prisonOffenderManager.getTeam()).isNotNull();
            assertThat(prisonOffenderManager.getStaffCode()).isEqualTo("BWIA010");
            assertThat(prisonOffenderManager.getStaffId()).isEqualTo(2500057541L);
            assertThat(prisonOffenderManager.getGrade()).isNull();
        }

        @Test
        void givenIncludeProbationAreaTeams_whenGetAllOffenderManagersByNOMSNumber_thenTeamsAvailable() {
            final var offenderManagers = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/G9542VP/allOffenderManagers?includeProbationAreaTeams=true")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager[].class);

            assertThat(offenderManagers).hasSize(2);

            final var communityOffenderManager = Stream.of(offenderManagers).filter(not(CommunityOrPrisonOffenderManager::getIsPrisonOffenderManager))
                .findAny().orElseThrow();
            final var prisonOffenderManager = Stream.of(offenderManagers).filter(CommunityOrPrisonOffenderManager::getIsPrisonOffenderManager)
                .findAny().orElseThrow();

            assertThat(communityOffenderManager.getProbationArea().getTeams()).hasSize(61);
            assertThat(prisonOffenderManager.getProbationArea().getTeams()).hasSize(5);
        }

        @Test
        void getAllOffenderManagersByNOMSNumberReturn404WhenOffenderDoesNotExist() {
            given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/DOESNOTEXIST/allOffenderManagers")
                .then()
                .statusCode(404);
        }
    }

    @Nested
    class allOffenderManagersByCrn {

        @Test
        void canGetAllOffenderManagersByCrn() {
            final var offenderManagers = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/CRN40/allOffenderManagers")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager[].class);

            assertThat(offenderManagers).hasSize(2);

            final var prisonOffenderManager = Stream.of(offenderManagers).filter(CommunityOrPrisonOffenderManager::getIsPrisonOffenderManager)
                .findAny().orElseThrow();
            final var communityOffenderManager = Stream.of(offenderManagers).filter(not(CommunityOrPrisonOffenderManager::getIsPrisonOffenderManager))
                .findAny().orElseThrow();

            assertThat(communityOffenderManager.getIsResponsibleOfficer()).isFalse();
            assertThat(communityOffenderManager.getIsUnallocated()).isFalse();
            assertThat(communityOffenderManager.getStaff().getEmail()).isEqualTo("jim.snow@justice.gov.uk");
            assertThat(communityOffenderManager.getStaff().getPhoneNumber()).isEqualTo("01512112121");
            assertThat(communityOffenderManager.getStaff().getForenames()).isEqualTo("JIM");
            assertThat(communityOffenderManager.getStaff().getSurname()).isEqualTo("SNOW");
            assertThat(communityOffenderManager.getProbationArea()).isNotNull();
            assertThat(communityOffenderManager.getProbationArea().getInstitution()).isNull();
            assertThat(communityOffenderManager.getProbationArea().getTeams()).isNull();
            assertThat(communityOffenderManager.getStaff()).isNotNull();
            assertThat(communityOffenderManager.getTeam()).isNotNull();
            assertThat(communityOffenderManager.getStaffCode()).isEqualTo("SH0007");
            assertThat(communityOffenderManager.getFromDate()).isEqualTo(LocalDate.of(2018, Month.MAY, 4));
            assertThat(communityOffenderManager.getGrade()).isNotNull();
            assertThat(communityOffenderManager.getGrade().getDescription()).isEqualTo("PO");
            assertThat(communityOffenderManager.getGrade().getCode()).isEqualTo("M");

            assertThat(prisonOffenderManager.getStaffCode()).isEqualTo("BEDUATU");
            assertThat(prisonOffenderManager.getIsResponsibleOfficer()).isFalse();
            assertThat(prisonOffenderManager.getIsUnallocated()).isTrue();
            assertThat(prisonOffenderManager.getTeam().getCode()).isEqualTo("BEDUAT");
            assertThat(prisonOffenderManager.getProbationArea().getCode()).isEqualTo("GCS");
            assertThat(prisonOffenderManager.getProbationArea().getTeams()).isNull();
            assertThat(prisonOffenderManager.getFromDate()).isEqualTo(LocalDate.of(2019, Month.JANUARY, 1));
            assertThat(prisonOffenderManager.getStaffId()).isEqualTo(101);
        }

        @Test
        void givenIncludeProbationAreaTeams_whenGetAllOffenderManagersByCrn_thenTeamsAvailable() {
            final var offenderManagers = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/CRN40/allOffenderManagers?includeProbationAreaTeams=true")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager[].class);

            assertThat(offenderManagers).hasSize(2);

            final var communityOffenderManager = Stream.of(offenderManagers).filter(not(CommunityOrPrisonOffenderManager::getIsPrisonOffenderManager))
                .findAny().orElseThrow();
            final var prisonOffenderManager = Stream.of(offenderManagers).filter(CommunityOrPrisonOffenderManager::getIsPrisonOffenderManager)
                .findAny().orElseThrow();

            assertThat(communityOffenderManager.getProbationArea().getTeams()).hasSize(12);
            assertThat(prisonOffenderManager.getProbationArea().getTeams()).hasSize(16);
        }

        @Test
        void givenUnknownCrn_whenGetAllOffenderManagersByCrn_thenReturn404() {
            given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/DOESNOTEXIST/allOffenderManagers")
                .then()
                .statusCode(404);
        }
    }

}
