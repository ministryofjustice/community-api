package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.justice.digital.delius.jpa.dao.OffenderDelta;
import uk.gov.justice.digital.delius.jpa.standard.entity.ManagementTier;
import uk.gov.justice.digital.delius.jpa.standard.entity.ManagementTierId;
import uk.gov.justice.digital.delius.jpa.standard.repository.StandardReferenceRepository;

import java.util.List;

import static io.restassured.RestAssured.given;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffendersResource_updateTier extends IntegrationTestBase {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StandardReferenceRepository standardReferenceRepository;

    @Test
    public void createsTier() {
        final var offenderId = 2500343964L;
        List<String> originalTier =  jdbcTemplate.query("SELECT CODE_VALUE FROM MANAGEMENT_TIER m \n" +
            "JOIN r_standard_reference_list s \n" +
            "ON m.tier_id=s.standard_reference_list_id\n" +
            "WHERE offender_id=2500343964", (resultSet, rowNum) ->
            resultSet.getString("CODE_VALUE"));

        assertThat(originalTier.get(0)).isEqualTo("UA2");

        given()
            .auth()
            .oauth2(tokenWithRoleManagementTierUpdate())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .post("/offenders/crn/X320741/tier/B1")
            .then()
            .statusCode(200);

        List<String> updatedTier =  jdbcTemplate.query("SELECT CODE_VALUE FROM MANAGEMENT_TIER m \n" +
            "JOIN r_standard_reference_list s \n" +
            "ON m.tier_id=s.standard_reference_list_id\n" +
            "WHERE offender_id=2500343964", (resultSet, rowNum) ->
            resultSet.getString("CODE_VALUE"));
        // final var updatedTierReason = standardReferenceRepository.findByCodeAndCodeSetName("ATS", "TIER_CHANGE_REASON").get();
        assertThat(updatedTier.contains("UB1")).isTrue();
       //  assertThat(updatedTier.get().getTierChangeReason()).isEqualTo(updatedTierReason);
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
