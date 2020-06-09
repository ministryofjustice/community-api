package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.justice.digital.delius.FlywayRestoreExtension;
import uk.gov.justice.digital.delius.data.api.UpdateCustody;

import java.util.List;

import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(FlywayRestoreExtension.class)
public class UserAuditingTest extends IntegrationTestBase {
    private static final String NOMS_NUMBER = "G9542VP";
    private static final String PRISON_BOOKING_NUMBER = "V74111";

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    public void willUseAPIUserByDefaultWhenNoUsernameSupplied() {
        final var token = createJwt("ROLE_COMMUNITY_CUSTODY_UPDATE", "ROLE_COMMUNITY");

        given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(createUpdateCustody("MDI"))
                .when()
                .put(format("offenders/nomsNumber/%s/custody/bookingNumber/%s", NOMS_NUMBER, PRISON_BOOKING_NUMBER))
                .then()
                .statusCode(200);

        //custody record should have been updated
        final var custodyRecord = jdbcTemplate.query(
                "SELECT u.DISTINGUISHED_NAME from CUSTODY c, USER_ u where c.PRISONER_NUMBER = ? AND c.LAST_UPDATED_USER_ID = u.USER_ID ORDER BY c.LAST_UPDATED_DATETIME DESC",
                List.of(PRISON_BOOKING_NUMBER).toArray(),
                new ColumnMapRowMapper()).get(0);
        assertThat(custodyRecord.get("DISTINGUISHED_NAME")).isEqualTo("APIUser");
    }

    @Test
    public void willUseSuppliedUsernameEvenWithClientCredentials() {
        final var token = createJwt("my-client-id", "andy.marke", 2500040507L, "ROLE_COMMUNITY_CUSTODY_UPDATE", "ROLE_COMMUNITY");

        given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(createUpdateCustody("WWI"))
                .when()
                .put(format("offenders/nomsNumber/%s/custody/bookingNumber/%s", NOMS_NUMBER, PRISON_BOOKING_NUMBER))
                .then()
                .statusCode(200);

        //custody record should have been updated
        final var custodyRecord = jdbcTemplate.query(
                "SELECT u.DISTINGUISHED_NAME from CUSTODY c, USER_ u where c.PRISONER_NUMBER = ? AND c.LAST_UPDATED_USER_ID = u.USER_ID ORDER BY c.LAST_UPDATED_DATETIME DESC",
                List.of(PRISON_BOOKING_NUMBER).toArray(),
                new ColumnMapRowMapper()).get(0);
        assertThat(custodyRecord.get("DISTINGUISHED_NAME")).isEqualTo("andy.marke");
    }

    @Test
    public void willUseSuppliedUsername() {
        final var token = createJwt("andy.marke", 2500040507L, "ROLE_COMMUNITY_CUSTODY_UPDATE", "ROLE_COMMUNITY");

        given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(createUpdateCustody("BXI"))
                .when()
                .put(format("offenders/nomsNumber/%s/custody/bookingNumber/%s", NOMS_NUMBER, PRISON_BOOKING_NUMBER))
                .then()
                .statusCode(200);

        //custody record should have been updated
        final var custodyRecord = jdbcTemplate.query(
                "SELECT u.DISTINGUISHED_NAME from CUSTODY c, USER_ u where c.PRISONER_NUMBER = ? AND c.LAST_UPDATED_USER_ID = u.USER_ID ORDER BY c.LAST_UPDATED_DATETIME DESC",
                List.of(PRISON_BOOKING_NUMBER).toArray(),
                new ColumnMapRowMapper()).get(0);
        assertThat(custodyRecord.get("DISTINGUISHED_NAME")).isEqualTo("andy.marke");
    }

    private String createJwt(String clientId, String username, Long userId, final String... roles) {
        return jwtAuthenticationHelper.createJwt(
                createJwtBuilder(roles)
                        .clientId(clientId)
                        .username(username)
                        .userId(userId.toString())
                        .build());
    }

    private String createJwt(String username, Long userId, final String... roles) {
        return jwtAuthenticationHelper.createJwt(
                createJwtBuilder(roles)
                        .username(username)
                        .name("John Smith")
                        .userId(userId.toString())
                        .build());
    }


    private String createUpdateCustody(String prisonCode) {
        return writeValueAsString(UpdateCustody
                .builder()
                .nomsPrisonInstitutionCode(prisonCode)
                .build());
    }

}
