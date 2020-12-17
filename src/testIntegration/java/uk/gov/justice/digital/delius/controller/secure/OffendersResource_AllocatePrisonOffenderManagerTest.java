package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.justice.digital.delius.FlywayRestoreExtension;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.Contact;
import uk.gov.justice.digital.delius.data.api.ContactableHuman;
import uk.gov.justice.digital.delius.data.api.CreatePrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.Human;
import uk.gov.justice.digital.delius.data.api.StaffDetails;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SuppressWarnings("SameParameterValue")
@ExtendWith(FlywayRestoreExtension.class)
public class OffendersResource_AllocatePrisonOffenderManagerTest extends IntegrationTestBase {
    @Autowired
    private JdbcTemplate jdbcTemplate;

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
                .get("staff/staffIdentifier/{staffId}", newPrisonOffenderManager.getStaffId())
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
        // GIVEN the offender is in Berwyn prison
        given()
                .auth().oauth2(tokenWithRoleCommunity())
                .when()
                .get("offenders/nomsNumber/{nomsNumber}/custody/bookingNumber/{bookingNumber}", "G9542VP", "V74111")
                .then()
                .statusCode(200)
                .body("institution.code", equalTo("BWIHMP"))
                .body("institution.description", equalTo("Berwyn (HMP)"))
        ;

        // AND there is an existing POM
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

        // WHEN a POM is allocated at Moorland prison
        final var newPrisonOffenderManager = given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf(ContactableHuman
                                .builder()
                                .surname("Marke")
                                .forenames("Joe")
                                .build(),
                        "MDI"))
                .when()
                .put("/offenders/nomsNumber/G9542VP/prisonOffenderManager")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager.class);

        //THEN
        // no longer previous POM
        assertThat(newPrisonOffenderManager.getStaffId()).isNotEqualTo(2500057541L);
        // AND but will be a staff code in BWI area with area prefix
        assertThat(newPrisonOffenderManager.getStaffCode()).startsWith("MDI");

        // AND OM Team will be POM team in area within the POM borough, district and LDU
        assertThat(newPrisonOffenderManager.getTeam().getDescription()).isEqualTo("Prison Offender Managers");
        assertThat(newPrisonOffenderManager.getTeam().getCode()).isEqualTo("MDIPOM");
        assertThat(newPrisonOffenderManager.getTeam().getBorough().getDescription()).isEqualTo("Prison Offender Managers");
        assertThat(newPrisonOffenderManager.getTeam().getBorough().getCode()).isEqualTo("MDIPOM");
        assertThat(newPrisonOffenderManager.getTeam().getDistrict().getDescription()).isEqualTo("Prison Offender Managers");
        assertThat(newPrisonOffenderManager.getTeam().getDistrict().getCode()).isEqualTo("MDIPOM");
        assertThat(newPrisonOffenderManager.getTeam().getLocalDeliveryUnit().getDescription()).isEqualTo("Prison Offender Managers");
        assertThat(newPrisonOffenderManager.getTeam().getLocalDeliveryUnit().getCode()).isEqualTo("MDIPOM");
        // AND OM will be in Prison Probation Area
        assertThat(newPrisonOffenderManager.getProbationArea().getDescription()).isEqualTo("Moorland (HMP & YOI)");
        assertThat(newPrisonOffenderManager.getProbationArea().getCode()).isEqualTo("MDI");

        final var staffDetails = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("staff/staffIdentifier/{staffId}", newPrisonOffenderManager.getStaffId())
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(StaffDetails.class);

        // AND New staff member will be in the POM Team
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

        // AND should have both community and prison offender manager
        assertThat(offenderManagers).hasSize(2);
        assertThat(prisonOffenderManager(offenderManagers)).isPresent();

        // AND responsible officer should be the new  POM
        assertThat(prisonOffenderManager(offenderManagers).orElseThrow().getIsResponsibleOfficer()).isTrue();
        assertThat(prisonOffenderManager(offenderManagers).orElseThrow().getStaffCode()).isEqualTo(newPrisonOffenderManager.getStaffCode());
        assertThat(communityOffenderManager(offenderManagers)).isPresent();

        // AND the custody record has a new location of Moorland
        given()
                .auth().oauth2(tokenWithRoleCommunity())
                .when()
                .get("offenders/nomsNumber/{nomsNumber}/custody/bookingNumber/{bookingNumber}", "G9542VP", "V74111")
                .then()
                .statusCode(200)
                .body("institution.code", equalTo("MDIHMP"))
                .body("institution.description", equalTo("Moorland (HMP & YOI)"))
        ;

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
    @Test
    @DisplayName("Case is normalized when adding a new staff member, but existing staff members with names in a non standard case will be honoured")
    public void shouldIgnoreCaseWhenAddingAPOM() {
        // When I assign a POM with a name in upper case
        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf(ContactableHuman.builder().forenames("JANE").surname("MACDONALD").build(), "BWI"))
                .when()
                .put("/offenders/nomsNumber/G4340UK/prisonOffenderManager")
                .then()
                .statusCode(200);

        // THEN the name will be saved as standard name capitalisation
        final var offenderManagers = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/G4340UK/allOffenderManagers")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager[].class);

        assertThat(prisonOffenderManager(offenderManagers).orElseThrow().getStaff()).isEqualTo(Human.builder().forenames("Jane").surname("Macdonald").build());

        // GIVEN the name was originally created with saved with a non-standard case
        jdbcTemplate.update("UPDATE STAFF SET FORENAME = ?,  SURNAME = ? WHERE STAFF_ID = ?", "Jane", "MacDonald", prisonOffenderManager(offenderManagers).orElseThrow().getStaffId());


        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf(ContactableHuman.builder().forenames("Someone").surname("Else").build(), "BWI"))
                .when()
                .put("/offenders/nomsNumber/G4340UK/prisonOffenderManager")
                .then()
                .statusCode(200);

        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf(ContactableHuman.builder().forenames("JANE").surname("MACDONALD").build(), "BWI"))
                .when()
                .put("/offenders/nomsNumber/G4340UK/prisonOffenderManager")
                .then()
                .statusCode(200);

        // THEN the name will be the original saved name
        assertThat(prisonOffenderManager(given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/G4340UK/allOffenderManagers")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager[].class)).orElseThrow().getStaff()).isEqualTo(Human.builder().forenames("Jane").surname("MacDonald").build());
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

    @Test
    @DisplayName("Will update the active offender when two found with same NOMS number")
    void willUpdateTheActiveOffenderWhenTwoFoundWithSameNOMSNumber() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf(ContactableHuman.builder().forenames("Bob").surname("Grindle").build(), "BWI"))
                .when()
                .put("/offenders/nomsNumber/G3232DD/prisonOffenderManager")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Will return a 409 conflict when supplying a NOMS number with duplicate active offenders")
    void willReturnAConflictWhenSupplyingANOMSNumberWithDuplicateActiveOffenders() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf(ContactableHuman.builder().forenames("Bob").surname("Grindle").build(), "BWI"))
                .when()
                .put("/offenders/nomsNumber/G3636DD/prisonOffenderManager")
                .then()
                .statusCode(409);
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

    private String createPrisonOffenderManagerOf(final ContactableHuman staff, final String nomsPrisonInstitutionCode) {
        return writeValueAsString(CreatePrisonOffenderManager
                .builder()
                .officer(staff)
                .nomsPrisonInstitutionCode(nomsPrisonInstitutionCode)
                .build());
    }


}
