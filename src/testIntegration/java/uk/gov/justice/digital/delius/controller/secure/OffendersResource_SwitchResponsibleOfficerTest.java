package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.justice.digital.delius.FlywayRestoreExtension;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.Contact;
import uk.gov.justice.digital.delius.data.api.CreatePrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.ResponsibleOfficerSwitch;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SuppressWarnings("SameParameterValue")
@ExtendWith(FlywayRestoreExtension.class)
public class OffendersResource_SwitchResponsibleOfficerTest extends IntegrationTestBase {
    @Test
    @DisplayName("Must have ROLE_COMMUNITY_CUSTODY_UPDATE")
    public void mustHaveUpdateRoleToSwitchRO() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createResponsibleOfficerSwitchOf(true))
                .when()
                .put("/offenders/nomsNumber/G0560UO/responsibleOfficer/switch")
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Must have valid token")
    public void mustHaveValidToken() {
        given()
                .auth()
                .oauth2("XX")
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createResponsibleOfficerSwitchOf(true))
                .when()
                .put("/offenders/nomsNumber/G0560UO/responsibleOfficer/switch")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Must supply noms number")
    public void mustSupplyNomsNumber() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createResponsibleOfficerSwitchOf(true))
                .when()
                .put("/offenders/nomsNumber//responsibleOfficer/switch")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Will switch Responsible officer from COM to POM and back again")
    public void canSwitchResponsibleOfficerToPrisonOffenderManager() {
        // Given an offender has no prison offender manager
        assertThat(getOffenderManagers("G0560UO")).hasSize(1);

        // Then the responsible officer can not be assigned to a POM
        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createResponsibleOfficerSwitchOf(false))
                .when()
                .put("/offenders/nomsNumber/G0560UO/responsibleOfficer/switch")
                .then()
                .statusCode(409);

        // Given an offender has Prisoner Offender Manager
        allocatePrisonerOffenderManager("G0560UO");

        // AND the COM is currently the Responsible Officer
        final var offenderManagersBeforeSwitch = getOffenderManagers("G0560UO");

        assertThat(offenderManagersBeforeSwitch).hasSize(2);
        assertThat(isCommunityOffenderManagerTheRO(offenderManagersBeforeSwitch)).isTrue();
        assertThat(isPrisonOffenderManagerTheRO(offenderManagersBeforeSwitch)).isFalse();

        // WHEN I request responsible officer is switched to the POM
        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createResponsibleOfficerSwitchOf(false))
                .when()
                .put("/offenders/nomsNumber/G0560UO/responsibleOfficer/switch")
                .then()
                .statusCode(200);

        // AND when I check who is current Responsible officer currently is
        final var offenderManagersAfterSwitch = getOffenderManagers("G0560UO");

        // THEN the responsible officer is now set to Prison Offender Manager
        assertThat(offenderManagersBeforeSwitch).hasSize(2);
        assertThat(isPrisonOffenderManagerTheRO(offenderManagersAfterSwitch)).isTrue();
        assertThat(isCommunityOffenderManagerTheRO(offenderManagersAfterSwitch)).isFalse();

        // AND a CONTACT has been added to show responsible officer has changed
        final var contacts = getRecentContacts("G0560UO");

        assertThat(Arrays
                .stream(contacts)
                .anyMatch(contact -> contact.getContactType().getDescription().equals("Responsible Officer Change"))
        ).isTrue();

        // AND when I request a switch back to the COM
        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createResponsibleOfficerSwitchOf(true))
                .when()
                .put("/offenders/nomsNumber/G0560UO/responsibleOfficer/switch")
                .then()
                .statusCode(200);

        // AND I check who is current Responsible officer is
        final var offenderManagersAfterSecondSwitch = getOffenderManagers("G0560UO");

        // THEN the responsible officer is now the Community Offender Manager
        assertThat(offenderManagersAfterSecondSwitch).hasSize(2);
        assertThat(isCommunityOffenderManagerTheRO(offenderManagersAfterSecondSwitch)).isTrue();
        assertThat(isPrisonOffenderManagerTheRO(offenderManagersAfterSecondSwitch)).isFalse();

    }

    @Test
    @DisplayName("Will respond with 404 if offender not found")
    public void shouldRespondWith404WhenSwitchingResponsibleOfficerForOffenderNotFound() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createResponsibleOfficerSwitchOf(false))
                .when()
                .put("/offenders/nomsNumber/BANANA/responsibleOfficer/switch")
                .then()
                .statusCode(404);
    }


    public Optional<CommunityOrPrisonOffenderManager> prisonOffenderManager(final CommunityOrPrisonOffenderManager[] offenderManagers) {
        return Stream.of(offenderManagers).filter(CommunityOrPrisonOffenderManager::getIsPrisonOffenderManager)
                .findAny();
    }

    public Optional<CommunityOrPrisonOffenderManager> communityOffenderManager(final CommunityOrPrisonOffenderManager[] offenderManagers) {
        return Stream.of(offenderManagers).filter(not(CommunityOrPrisonOffenderManager::getIsPrisonOffenderManager))
                .findAny();
    }


    private String createResponsibleOfficerSwitchOf(final boolean switchToCOM) {
        return writeValueAsString(ResponsibleOfficerSwitch
                .builder()
                .switchToCommunityOffenderManager(switchToCOM)
                .switchToPrisonOffenderManager(!switchToCOM)
                .build());
    }


    private String createPrisonOffenderManager() {
        return writeValueAsString(CreatePrisonOffenderManager
                .builder()
                .staffId(2_500_057_541L)
                .nomsPrisonInstitutionCode("BWI")
                .build());
    }

    private void allocatePrisonerOffenderManager(String nomsNumber) {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManager())
                .when()
                .put(String.format("/offenders/nomsNumber/%s/prisonOffenderManager", nomsNumber))
                .then()
                .statusCode(200);
    }

    private CommunityOrPrisonOffenderManager[] getOffenderManagers(String nomsNumber) {
        return given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format("/offenders/nomsNumber/%s/allOffenderManagers", nomsNumber))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager[].class);
    }

    private Contact[] getRecentContacts(String nomsNumber) {
        return given()
                .when()
                .header("Authorization", legacyToken())
                .queryParam("from", LocalDate.now().atStartOfDay().toString())
                .basePath("/api")
                .get(String.format("/offenders/nomsNumber/%s/contacts", nomsNumber))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Contact[].class);
    }

    private Boolean isPrisonOffenderManagerTheRO(CommunityOrPrisonOffenderManager[] offenderManagersAfterSecondSwitch) {
        return prisonOffenderManager(offenderManagersAfterSecondSwitch).orElseThrow().getIsResponsibleOfficer();
    }

    private Boolean isCommunityOffenderManagerTheRO(CommunityOrPrisonOffenderManager[] offenderManagersBeforeSwitch) {
        return communityOffenderManager(offenderManagersBeforeSwitch).orElseThrow().getIsResponsibleOfficer();
    }

}
