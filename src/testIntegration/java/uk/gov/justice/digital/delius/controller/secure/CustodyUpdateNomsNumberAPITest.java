package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.digital.delius.FlywayRestoreExtension;
import uk.gov.justice.digital.delius.data.api.IDs;
import uk.gov.justice.digital.delius.data.api.UpdateOffenderNomsNumber;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({SpringExtension.class, FlywayRestoreExtension.class})
public class CustodyUpdateNomsNumberAPITest extends IntegrationTestBase {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void updateNomsNumber() {
        final var token = createJwt("ROLE_COMMUNITY_CUSTODY_UPDATE", "ROLE_COMMUNITY");
        // Given Offender with CRN = X320741 has NOMS_NUMBER = G9542VP

        // When I assign NOMS_NUMBER = G9542VP to Offender with CRN = CRN11
        final var iDs = given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(createUpdateNomsNumber("G9542VP"))
                .when()
                .put(String.format("offenders/crn/%s/nomsNumber", "CRN11"))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(IDs.class);


        // The Offender with now have the NOMS_NUMBER assigned
        assertThat(iDs.getNomsNumber()).isEqualTo("G9542VP");

        // AND previous owner of NOMS number will have their NOMS number moved to an additional identifier
        final var additionalIdentifier = jdbcTemplate.query(
                "SELECT ad.IDENTIFIER, o.NOMS_NUMBER, srl.CODE_VALUE from ADDITIONAL_IDENTIFIER ad, OFFENDER o, R_STANDARD_REFERENCE_LIST srl  where o.CRN = ? and o.OFFENDER_ID = ad.OFFENDER_ID and ad.IDENTIFIER_NAME_ID = srl.STANDARD_REFERENCE_LIST_ID order by ad.CREATED_DATETIME desc ",
                List.of("X320741").toArray(),
                new ColumnMapRowMapper())
                .stream()
                .findFirst()
                .orElseThrow();


        assertThat(additionalIdentifier.get("IDENTIFIER")).isEqualTo("G9542VP");
        assertThat(additionalIdentifier.get("CODE_VALUE")).isEqualTo("DNOMS");
        assertThat(additionalIdentifier.get("NOMS_NUMBER")).isNull();

        // AND formar NOMS number will moved to an additional identifier
        final var newAdditionalIdentifier = jdbcTemplate.query(
                "SELECT ad.IDENTIFIER, o.NOMS_NUMBER, srl.CODE_VALUE from ADDITIONAL_IDENTIFIER ad, OFFENDER o, R_STANDARD_REFERENCE_LIST srl  where o.CRN = ? and o.OFFENDER_ID = ad.OFFENDER_ID and ad.IDENTIFIER_NAME_ID = srl.STANDARD_REFERENCE_LIST_ID order by ad.CREATED_DATETIME desc",
                List.of("CRN11").toArray(),
                new ColumnMapRowMapper())
                .stream()
                .findFirst()
                .orElseThrow();


        // from initial seed data
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
