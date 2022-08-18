package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.digital.delius.FlywayRestoreExtension;
import uk.gov.justice.digital.delius.data.api.Contact;
import uk.gov.justice.digital.delius.jpa.filters.ContactFilter;
import uk.gov.justice.digital.delius.jpa.standard.entity.ManagementTier;
import uk.gov.justice.digital.delius.jpa.standard.repository.ManagementTierRepository;
import uk.gov.justice.digital.delius.service.ContactService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(FlywayRestoreExtension.class)
public class OffendersResource_updateTier extends IntegrationTestBase {

    @Autowired
    private ManagementTierRepository managementTierRepository;

    @Autowired
    private ContactService contactService;

    private ContactFilter contactFilter = ContactFilter.builder()
        .contactTypes(Optional.of(Collections.singletonList("ETCH20")))
        .build();

    @Test
    public void createsContactWhenTierUpdated() {

        List<Contact> contacts = contactService.contactsFor(2500343964L, contactFilter);
        assertThat(contacts.isEmpty()).isTrue();

        given()
            .auth()
            .oauth2(tokenWithRoleManagementTierUpdate())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .post("/offenders/crn/X320741/tier/B1")
            .then()
            .statusCode(200);

        List<ManagementTier> updatedTiers = managementTierRepository.findAll();

        ManagementTier tierB1 = updatedTiers.get(1);
        assertThat(tierB1.getId().getTier().getCodeValue()).isEqualTo("UB1");
        assertThat(tierB1.getTierChangeReason().getCodeValue()).isEqualTo("ATS");

        List<Contact> updatedContacts = contactService.contactsFor(2500343964L, contactFilter);

        assertThat(updatedContacts.stream().anyMatch(c ->  c.getNotes().contains("Tier: B-1"))).isTrue();
        managementTierRepository.deleteById(tierB1.getId());
    }

    @Test
    public void createsTierWhenNoTierExists() {
        given()
            .auth()
            .oauth2(tokenWithRoleManagementTierUpdate())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .post("/offenders/crn/X330899/tier/B1")
            .then()
            .statusCode(200);

        List<ManagementTier> updatedTiers = managementTierRepository.findAll();

        ManagementTier tierB1 = updatedTiers.get(1);
        assertThat(tierB1.getId().getTier().getCodeValue()).isEqualTo("UB1");
        assertThat(tierB1.getTierChangeReason().getCodeValue()).isEqualTo("ATS");
    }


    @Test
    public void noTierInsertedWhenTierIsSame() {

        given()
            .auth()
            .oauth2(tokenWithRoleManagementTierUpdate())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .post("/offenders/crn/X320741/tier/A2")
            .then()
            .statusCode(200);

        List<ManagementTier> updatedTiers = managementTierRepository.findAll();
        assertThat(updatedTiers.size()).isEqualTo(1);
        ManagementTier tierA2 = updatedTiers.get(0);
        assertThat(tierA2.getId().getTier().getCodeValue()).isEqualTo("UA2");
        assertThat(tierA2.getTierChangeReason().getCodeValue()).isEqualTo("ATS");

    }

    @Test
    public void updateTierFails_offenderNotFound_returnsNotFound() {
        given()
            .auth()
            .oauth2(tokenWithRoleManagementTierUpdate())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .post("/offenders/crn/XNOTFOUND/tier/B1")
            .then()
            .statusCode(404);
    }

    @Test
    public void updateTierFails_offenderSoftDeleted_returnsNotFound() {
        given()
            .auth()
            .oauth2(tokenWithRoleManagementTierUpdate())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .post("/offenders/crn/CRN31/tier/B1")
            .then()
            .statusCode(404)
        .body("developerMessage", equalTo("Offender with CRN CRN31 not found"));
    }

    @Test
    public void updateTierFails_wrongRole_returnsForbidden() {
        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .post("/offenders/crn/X320741/tier/B1")
            .then()
            .statusCode(403);
    }

    @Test
    public void updateTierFails_tierNotFound_returns404_noContactWritten() {
        given()
            .auth()
            .oauth2(tokenWithRoleManagementTierUpdate())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .post("/offenders/crn/X320741/tier/NOTFOUND")
            .then()
            .statusCode(404);
        List<Contact> updatedContacts = contactService.contactsFor(2500343964L, contactFilter);
        assertThat(updatedContacts.stream().anyMatch(c -> c.getNotes().contains("NOTFOUND"))).isFalse();
    }


}
