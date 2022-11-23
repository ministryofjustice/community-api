package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.justice.digital.delius.FlywayRestoreExtension;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.CreatePrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.ResponsibleOfficerSwitch;

import java.util.Optional;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static java.util.function.Predicate.not;
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

    private Boolean isPrisonOffenderManagerTheRO(CommunityOrPrisonOffenderManager[] offenderManagersAfterSecondSwitch) {
        return prisonOffenderManager(offenderManagersAfterSecondSwitch).orElseThrow().getIsResponsibleOfficer();
    }

    private Boolean isCommunityOffenderManagerTheRO(CommunityOrPrisonOffenderManager[] offenderManagersBeforeSwitch) {
        return communityOffenderManager(offenderManagersBeforeSwitch).orElseThrow().getIsResponsibleOfficer();
    }

}
