package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.justice.digital.delius.FlywayRestoreExtension;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.Contact;
import uk.gov.justice.digital.delius.data.api.CreatePrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.Human;
import uk.gov.justice.digital.delius.data.api.StaffDetails;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(FlywayRestoreExtension.class)
public class OffendersResource_AllocatePrisonOffenderManagerTest extends IntegrationTestBase {
    @Test
    public void mustHaveUpdateRoleToAllocatedPOM() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf(2500057541L, "BWI"))
                .when()
                .put("/offenders/nomsNumber/G0560UO/prisonOffenderManager")
                .then()
                .statusCode(403);
    }

    @Test
    public void canAllocatePrisonOffenderManagersByNOMSNumberAndStaffId() {
        final var offenderManagersBeforeAllocation = given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/G0560UO/allOffenderManagers")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager[].class);

        assertThat(prisonOffenderManager(offenderManagersBeforeAllocation)).isNotPresent();
        assertThat(communityOffenderManager(offenderManagersBeforeAllocation)).isPresent();
        assertThat(communityOffenderManager(offenderManagersBeforeAllocation).orElseThrow().getIsResponsibleOfficer()).isTrue();

        final var newPrisonOffenderManager = given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf(2500057541L, "BWI"))
                .when()
                .put("/offenders/nomsNumber/G0560UO/prisonOffenderManager")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager.class);

        assertThat(newPrisonOffenderManager.getStaffId()).isEqualTo(2500057541L);

        // OM Team will be POM team in area within the POM borough, district and LDU
        assertThat(newPrisonOffenderManager.getTeam().getDescription()).isEqualTo("Prison Offender Managers");
        assertThat(newPrisonOffenderManager.getTeam().getCode()).isEqualTo("BWIPOM");
        assertThat(newPrisonOffenderManager.getTeam().getBorough().getDescription()).isEqualTo("Prison Offender Managers");
        assertThat(newPrisonOffenderManager.getTeam().getBorough().getCode()).isEqualTo("BWIPOM");
        assertThat(newPrisonOffenderManager.getTeam().getDistrict().getDescription()).isEqualTo("Prison Offender Managers");
        assertThat(newPrisonOffenderManager.getTeam().getDistrict().getCode()).isEqualTo("BWIPOM");
        assertThat(newPrisonOffenderManager.getTeam().getLocalDeliveryUnit().getDescription()).isEqualTo("Prison Offender Managers");
        assertThat(newPrisonOffenderManager.getTeam().getLocalDeliveryUnit().getCode()).isEqualTo("BWIPOM");
        // OM will be in Prison Probation Area
        assertThat(newPrisonOffenderManager.getProbationArea().getDescription()).isEqualTo("Berwyn (HMP)");
        assertThat(newPrisonOffenderManager.getProbationArea().getCode()).isEqualTo("BWI");

        final var staffDetails = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("staff/staffCode/BWIA010")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(StaffDetails.class);

        // Staff will now be in the POM Team
        assertThat(staffDetails.getTeams()).contains(newPrisonOffenderManager.getTeam());

        final var offenderManagers = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/G0560UO/allOffenderManagers")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager[].class);

        // should have both community and prison offender manager
        assertThat(prisonOffenderManager(offenderManagers)).isPresent();
        assertThat(communityOffenderManager(offenderManagers)).isPresent();
        // responsible officer remains unchanged
        assertThat(communityOffenderManager(offenderManagers).orElseThrow().getIsResponsibleOfficer()).isTrue();
        assertThat(prisonOffenderManager(offenderManagers).orElseThrow().getIsResponsibleOfficer()).isFalse();
        assertThat(prisonOffenderManager(offenderManagers).orElseThrow().getStaffId()).isEqualTo(2500057541L);
    }

    @Test
    public void canAllocatePrisonOffenderManagersByNOMSNumberAndStaffName() {
        final var offenderManagersBeforeAllocation = given()
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

        // assert we have a COM and POM with POM currently responsible officer
        assertThat(offenderManagersBeforeAllocation).hasSize(2);
        assertThat(prisonOffenderManager(offenderManagersBeforeAllocation)).isPresent();
        assertThat(prisonOffenderManager(offenderManagersBeforeAllocation).orElseThrow().getIsResponsibleOfficer()).isTrue();
        assertThat(prisonOffenderManager(offenderManagersBeforeAllocation).orElseThrow().getStaffId()).isEqualTo(2500057541L);
        assertThat(communityOffenderManager(offenderManagersBeforeAllocation)).isPresent();

        final var newPrisonOffenderManager = given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf(Human
                                .builder()
                                .surname("Marke")
                                .forenames("Joe")
                                .build(),
                        "BWI"))
                .when()
                .put("/offenders/nomsNumber/G9542VP/prisonOffenderManager")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager.class);

        // no longer previous POM
        assertThat(newPrisonOffenderManager.getStaffCode()).isNotEqualTo(2500057541L);
        // but will be a staff code in BWI area with area prefix
        assertThat(newPrisonOffenderManager.getStaffCode()).startsWith("BWI");

        // OM Team will be POM team in area within the POM borough, district and LDU
        assertThat(newPrisonOffenderManager.getTeam().getDescription()).isEqualTo("Prison Offender Managers");
        assertThat(newPrisonOffenderManager.getTeam().getCode()).isEqualTo("BWIPOM");
        assertThat(newPrisonOffenderManager.getTeam().getBorough().getDescription()).isEqualTo("Prison Offender Managers");
        assertThat(newPrisonOffenderManager.getTeam().getBorough().getCode()).isEqualTo("BWIPOM");
        assertThat(newPrisonOffenderManager.getTeam().getDistrict().getDescription()).isEqualTo("Prison Offender Managers");
        assertThat(newPrisonOffenderManager.getTeam().getDistrict().getCode()).isEqualTo("BWIPOM");
        assertThat(newPrisonOffenderManager.getTeam().getLocalDeliveryUnit().getDescription()).isEqualTo("Prison Offender Managers");
        assertThat(newPrisonOffenderManager.getTeam().getLocalDeliveryUnit().getCode()).isEqualTo("BWIPOM");
        // OM will be in Prison Probation Area
        assertThat(newPrisonOffenderManager.getProbationArea().getDescription()).isEqualTo("Berwyn (HMP)");
        assertThat(newPrisonOffenderManager.getProbationArea().getCode()).isEqualTo("BWI");

        final var staffDetails = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format("staff/staffCode/%s", newPrisonOffenderManager.getStaffCode()))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(StaffDetails.class);

        // New staff member will be in the POM Team
        assertThat(staffDetails.getTeams()).contains(newPrisonOffenderManager.getTeam());


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

        // should have both community and prison offender manager
        assertThat(offenderManagers).hasSize(2);
        assertThat(prisonOffenderManager(offenderManagers)).isPresent();

        // responsible officer should be the new  POM
        assertThat(prisonOffenderManager(offenderManagers).orElseThrow().getIsResponsibleOfficer()).isTrue();
        assertThat(prisonOffenderManager(offenderManagers).orElseThrow().getStaffCode()).isEqualTo(newPrisonOffenderManager.getStaffCode());
        assertThat(communityOffenderManager(offenderManagers)).isPresent();
    }

    @Test
    public void willAddAContactWhenAllocatingPrisonOffenderManager() {
        final var justBeforeAllocation = LocalDateTime.now().minusHours(1);

        final var newPrisonOffenderManager = given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf(2500057541L, "BWI"))
                .when()
                .put("/offenders/nomsNumber/G4106UN/prisonOffenderManager")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager.class);

        assertThat(newPrisonOffenderManager.getStaffId()).isEqualTo(2500057541L);

        final var contacts = given()
                .when()
                .header("Authorization", legacyToken())
                .queryParam("from", justBeforeAllocation.toString())
                .basePath("/api")
                .get("/offenders/nomsNumber/G4106UN/contacts")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Contact[].class);

        assertThat(contacts).hasSize(1);
        assertThat(contacts[0].getContactType().getDescription()).isEqualTo("Prison Offender Manager - Automatic Transfer");
    }

    public Optional<CommunityOrPrisonOffenderManager> prisonOffenderManager(final CommunityOrPrisonOffenderManager[] offenderManagers) {
        return Stream.of(offenderManagers).filter(CommunityOrPrisonOffenderManager::getIsPrisonOffenderManager).findAny();
    }

    public Optional<CommunityOrPrisonOffenderManager> communityOffenderManager(final CommunityOrPrisonOffenderManager[] offenderManagers) {
        return Stream.of(offenderManagers).filter(not(CommunityOrPrisonOffenderManager::getIsPrisonOffenderManager)).findAny();
    }

    @Test
    public void shouldRespondWith404WhenAllocatingPrisonOffenderManagersAndExistingStaffNotFound() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf(234L))
                .when()
                .put("/offenders/nomsNumber/G9542VP/prisonOffenderManager")
                .then()
                .statusCode(404);
    }

    @Test
    public void shouldRespondWith404WhenAllocatingPrisonOffenderManagersAndOffenderNotFound() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf(2500057541L))
                .when()
                .put("/offenders/nomsNumber/DOESNOTEXIST/prisonOffenderManager")
                .then()
                .statusCode(404);
    }

    @Test
    public void shouldRespondWith400WhenAllocatingPrisonOffenderManagersAndPrisonInstitutionNotFound() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf(2500057541L, "DOESNOTEXIST"))
                .when()
                .put("/offenders/nomsNumber/G9542VP/prisonOffenderManager")
                .then()
                .statusCode(400);
    }

    @Test
    public void shouldRespondWith400WhenStaffMemberNotInThePrisonInstitutionProbationArea() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf(2500057541L, "WWI"))
                .when()
                .put("/offenders/nomsNumber/G9542VP/prisonOffenderManager")
                .then()
                .statusCode(400);
    }

    private String createPrisonOffenderManagerOf(final Long staffId) {
        return writeValueAsString(CreatePrisonOffenderManager
                .builder()
                .staffId(staffId)
                .nomsPrisonInstitutionCode("BWI")
                .build());
    }

    private String createPrisonOffenderManagerOf(final Long staffId, final String nomsPrisonInstitutionCode) {
        return writeValueAsString(CreatePrisonOffenderManager
                .builder()
                .staffId(staffId)
                .nomsPrisonInstitutionCode(nomsPrisonInstitutionCode)
                .build());
    }

    private String createPrisonOffenderManagerOf(final Human staff, final String nomsPrisonInstitutionCode) {
        return writeValueAsString(CreatePrisonOffenderManager
                .builder()
                .officer(staff)
                .nomsPrisonInstitutionCode(nomsPrisonInstitutionCode)
                .build());
    }


}
