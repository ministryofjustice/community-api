package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.digital.delius.FlywayRestoreExtension;
import uk.gov.justice.digital.delius.data.api.UpdateOffenderNomsNumber;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({SpringExtension.class, FlywayRestoreExtension.class})
public class CustodyReplaceNomsNumberAPITest extends IntegrationTestBase {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void mustHaveCommunityUpdateRole() {
        final var token = createJwt("ROLE_COMMUNITY");

        given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(createUpdateNomsNumber("G9992VP"))
                .when()
                .put(String.format("offenders/nomsNumber/%s/nomsNumber", "G0560UO"))
                .then()
                .statusCode(403);
    }


    @Test
    @DisplayName("Will not update noms number when new number already exists on another offender")
    public void replaceNomsNumberWilNotUpdateWhenNomsNumberAlreadyExists() {
        final var token = createJwt("ROLE_COMMUNITY_CUSTODY_UPDATE");
        // Given Offender with CRN = CRN11 has NOMS_NUMBER = G0560UO
        // And Offender with CRN = X320741 has NOMS_NUMBER = G9542VP

        // When I attempt to replace NOMS_NUMBER = G9542VP to Offender with original  = G0560UO
        final var existingCrn = given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(createUpdateNomsNumber("G9542VP"))
                .when()
                .put(String.format("offenders/nomsNumber/%s/nomsNumber", "G0560UO"))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath()
                .get("[0].crn");


        // Then Offender returned will be the offender that already had the noms number
        assertThat(existingCrn).isEqualTo("X320741");
    }

    @Test
    @DisplayName("Will update noms number replacing the existing noms number")
    public void replaceNomsNumber() {
        final var token = createJwt("ROLE_COMMUNITY_CUSTODY_UPDATE");
        // Given Offender with CRN = CRN11 has NOMS_NUMBER = G0560UO

        // When I replace NOMS_NUMBER = G9992VP to Offender with original  = G0560UO
        final var newNomsNumber = given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(createUpdateNomsNumber("G9992VP"))
                .when()
                .put(String.format("offenders/nomsNumber/%s/nomsNumber", "G0560UO"))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath()
                .get("[0].nomsNumber");


        // The Offender with now have the NOMS_NUMBER assigned
        assertThat(newNomsNumber).isEqualTo("G9992VP");

        // AND this offenders former NOMS number will moved to an additional identifier
        final var newAdditionalIdentifier = jdbcTemplate.query(
                "SELECT ad.IDENTIFIER, o.NOMS_NUMBER, srl.CODE_VALUE from ADDITIONAL_IDENTIFIER ad, OFFENDER o, R_STANDARD_REFERENCE_LIST srl  where o.CRN = ? and o.OFFENDER_ID = ad.OFFENDER_ID and ad.IDENTIFIER_NAME_ID = srl.STANDARD_REFERENCE_LIST_ID order by ad.CREATED_DATETIME desc",
                List.of("CRN11").toArray(),
                new ColumnMapRowMapper())
                .stream()
                .findFirst()
                .orElseThrow();

        assertThat(newAdditionalIdentifier.get("IDENTIFIER")).isEqualTo("G0560UO");
        assertThat(newAdditionalIdentifier.get("CODE_VALUE")).isEqualTo("XNOMS");
    }

    private String createUpdateNomsNumber(String nomsNumber) {
        return writeValueAsString(UpdateOffenderNomsNumber
                .builder()
                .nomsNumber(nomsNumber)
                .build());
    }

}
