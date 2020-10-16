package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.justice.digital.delius.FlywayRestoreExtension;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.Contact;
import uk.gov.justice.digital.delius.data.api.ContactType;
import uk.gov.justice.digital.delius.data.api.CreatePrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.StaffHuman;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(FlywayRestoreExtension.class)
public class OffendersResource_deallocatePomByNomsTest extends IntegrationTestBase {

    @Test
    public void badAuth_returnsUnauthorized() {
        given()
                .auth()
                .oauth2("BAD_TOKEN")
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .delete("/offenders/nomsNumber/G0560UO/prisonOffenderManager")
                .then()
                .statusCode(401);
    }

    @Test
    public void missingToken_returnsForbidden() {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .delete("/offenders/nomsNumber/G0560UO/prisonOffenderManager")
                .then()
                .statusCode(403);
    }

    @Test
    public void noRole_returnsForbidden() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .delete("/offenders/nomsNumber/G0560UO/prisonOffenderManager")
                .then()
                .statusCode(403);
    }

    @Test
    public void missingNomsNumber_returnsBadRequest() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .delete("/offenders/nomsNumber//prisonOffenderManager")
                .then()
                .statusCode(400);
    }

    @Test
    public void offenderHasNoPom_returnsConflict() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .delete("/offenders/nomsNumber/G9643VP/prisonOffenderManager")
                .then()
                .statusCode(409);
    }

    @Test
    public void offenderNotFound_returnsNotFound() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .delete("/offenders/nomsNumber/DOES_NOT_EXIST/prisonOffenderManager")
                .then()
                .statusCode(404);
    }

    @Test
    public void offenderPomExists_pomDeallocated() {
        allocateUserPom("G0560UO");

        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .delete("/offenders/nomsNumber/G0560UO/prisonOffenderManager")
                .then()
                .statusCode(200);

        final var pom = getPom("G0560UO");
        assertThat(pom.getIsUnallocated()).isTrue();
        assertThat(pom.getFromDate()).isEqualTo(LocalDate.now());
        assertThat(pom.getProbationArea().getCode()).isEqualTo("BWI");
        assertThat(pom.getIsResponsibleOfficer()).isFalse(); // Responsible officer is not changed

        // There should be 2 contacts - allocate assign POM, deallocate assign POM
        final var recentContacts = getRecentContacts("G0560UO");
        final var contactStaff = Arrays.stream(recentContacts).map(Contact::getStaff).collect(Collectors.toList());
        final var contactTypes = Arrays.stream(recentContacts).map(Contact::getContactType).collect(Collectors.toList());
        assertThat(contactStaff).extracting(StaffHuman::isUnallocated).containsExactly(false, true);
        assertThat(contactTypes).extracting(ContactType::getCode).containsExactly("EPOMAT", "EPOMAT");

        // Check idempotency of endpoint
        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .delete("/offenders/nomsNumber/G0560UO/prisonOffenderManager")
                .then()
                .statusCode(200);

        // No new contacts have been created
        assertThat(getRecentContacts("G0560UO").length).isEqualTo(2);

    }

    @Test
    public void offenderPomIsResponsibleOfficer_deallocatedPomStillResponsible() {
        allocateUserPom("G9542VP");

        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .delete("/offenders/nomsNumber/G9542VP/prisonOffenderManager")
                .then()
                .statusCode(200);

        final var pom = getPom("G9542VP");
        assertThat(pom.getIsUnallocated()).isTrue();
        assertThat(pom.getIsResponsibleOfficer()).isTrue(); // Responsible officer is changed

        // There should be 4 contacts - allocate assign POM, allocate assign RO, deallocate assign POM, deallocate assign RO
        final var recentContacts = getRecentContacts("G9542VP");
        final var contactStaff = Arrays.stream(recentContacts).map(Contact::getStaff).collect(Collectors.toList());
        final var contactTypes = Arrays.stream(recentContacts).map(Contact::getContactType).collect(Collectors.toList());
        assertThat(contactStaff).extracting(StaffHuman::isUnallocated).containsExactly(false, false, true, true);
        assertThat(contactTypes).extracting(ContactType::getCode).containsExactly("EPOMIN", "ROC", "EPOMAT", "ROC");
    }

    private void allocateUserPom(final String nomsNumber) {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerOf(2500057541L, "BWI"))
                .when()
                .put(format("/offenders/nomsNumber/%s/prisonOffenderManager", nomsNumber))
                .then()
                .statusCode(200);

        final CommunityOrPrisonOffenderManager pomBeforeDeallocate = getPom(nomsNumber);
        assertThat(pomBeforeDeallocate.getStaff().getForenames()).isEqualTo("User");
        assertThat(pomBeforeDeallocate.getStaff().getSurname()).isEqualTo("POM");
    }

    private CommunityOrPrisonOffenderManager getPom(final String nomsNumber) {
        final var offenderManagers = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(format("/offenders/nomsNumber/%s/allOffenderManagers", nomsNumber))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager[].class);

        return Arrays.stream(offenderManagers)
                .filter(CommunityOrPrisonOffenderManager::getIsPrisonOffenderManager)
                .findFirst()
                .orElseThrow();
    }

    private Contact[] getRecentContacts(final String nomsNumber ) {
        return given()
                .when()
                .header("Authorization", legacyToken())
                .queryParam("from", LocalDateTime.now().minusHours(1).toString())
                .basePath("/api")
                .get(format("/offenders/nomsNumber/%s/contacts", nomsNumber))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Contact[].class);
    }

    private String createPrisonOffenderManagerOf(final Long staffId, final String nomsPrisonInstitutionCode) {
        return writeValueAsString(CreatePrisonOffenderManager
                .builder()
                .staffId(staffId)
                .nomsPrisonInstitutionCode(nomsPrisonInstitutionCode)
                .build());
    }

}
