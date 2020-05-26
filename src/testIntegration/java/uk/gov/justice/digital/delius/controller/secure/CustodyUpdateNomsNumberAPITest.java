package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.digital.delius.FlywayRestoreExtension;
import uk.gov.justice.digital.delius.JwtAuthenticationHelper;
import uk.gov.justice.digital.delius.data.api.IDs;
import uk.gov.justice.digital.delius.data.api.UpdateOffenderNomsNumber;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith({SpringExtension.class, FlywayRestoreExtension.class})
@ActiveProfiles("dev-seed")
public class CustodyUpdateNomsNumberAPITest {

    @Autowired
    protected JwtAuthenticationHelper jwtAuthenticationHelper;
    @LocalServerPort
    int port;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
    }

    @Test
    public void updateNomsNumber() throws JsonProcessingException {
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

    private String createUpdateNomsNumber(String nomsNumber) throws JsonProcessingException {
        return objectMapper.writeValueAsString(UpdateOffenderNomsNumber
                .builder()
                .nomsNumber(nomsNumber)
                .build());
    }

    private String createJwt(final String... roles) {
        return jwtAuthenticationHelper.createJwt(JwtAuthenticationHelper.JwtParameters.builder()
                .username("APIUser")
                .roles(List.of(roles))
                .scope(Arrays.asList("read", "write"))
                .expiryTime(Duration.ofDays(1))
                .build());
    }


}
