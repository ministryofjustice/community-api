package uk.gov.justice.digital.delius.controller.secure;

import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffendersResource_updateTier extends IntegrationTestBase {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void createsTier() {
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

        List<Result> updatedTier =  jdbcTemplate.query("SELECT s.CODE_VALUE AS TIER, r.CODE_VALUE AS REASON FROM MANAGEMENT_TIER m \n" +
            "JOIN r_standard_reference_list s \n" +
            "ON m.tier_id=s.standard_reference_list_id \n" +
            "JOIN r_standard_reference_list r \n" +
            "ON m.tier_change_reason_id=r.standard_reference_list_id \n" +
            "WHERE offender_id=2500343964", (resultSet, rowNum) ->  Result.builder()
                .tier(resultSet.getString("TIER"))
                .reason(resultSet.getString("REASON"))
                .build());

        Result expectedTier = Result.builder().tier("UB1").reason("ATS").build();
        assertThat(updatedTier.contains(expectedTier)).isTrue();
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

@Data
@Builder
class Result {
    String tier;
    String reason;
}