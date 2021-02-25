package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.digital.delius.jpa.standard.entity.ManagementTier;
import uk.gov.justice.digital.delius.jpa.standard.repository.ManagementTierRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StandardReferenceRepository;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffendersResource_updateTier extends IntegrationTestBase {

    @Autowired
    private ManagementTierRepository managementTierRepository;

    @Autowired
    private StandardReferenceRepository standardReferenceRepository;

    @Test
    public void createsTier() {
        final var offenderId = 2500343964L;
        final var originalTier = managementTierRepository.findFirstByOffenderIdAndSoftDeletedEqualsOrderByDateChangedDesc(offenderId,0);

        assertThat(originalTier.get().getTier().getCodeValue()).isEqualTo("UA2");

        given()
            .auth()
            .oauth2(tokenWithRoleManagementTierUpdate())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .post("/offenders/crn/X320741/tier/B1")
            .then()
            .statusCode(200);

        final var updatedTier = managementTierRepository.findFirstByOffenderIdAndSoftDeletedEqualsOrderByDateChangedDesc(offenderId,0);
        final var updatedTierReason = standardReferenceRepository.findByCodeAndCodeSetName("ATS", "TIOER_CHANGE_REASON").get();
        assertThat(updatedTier.get().getTier().getCodeValue()).isEqualTo("UB1");
        assertThat(updatedTier.get().getTierChangeReason()).isEqualTo(updatedTierReason);
    }


    @Test
    public void updatesTier_offenderNotFound_returnsNotFound() {
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
    public void updatesTier_wrongRole_returnsForbidden() {
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
    public void updatesTier_tierNotFound_returns404() {
        given()
            .auth()
            .oauth2(tokenWithRoleManagementTierUpdate())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .post("/offenders/crn/X320741/tier/NOTFOUND")
            .then()
            .statusCode(404);
    }

}
