package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.digital.delius.FlywayRestoreExtension;
import uk.gov.justice.digital.delius.data.api.NewCourtDto;
import uk.gov.justice.digital.delius.data.api.UpdateCourtDto;

import java.util.List;

import static io.restassured.RestAssured.given;

@ExtendWith({SpringExtension.class, FlywayRestoreExtension.class})
public class CourtAPITest extends IntegrationTestBase {
    private static final String NEW_COURT_CODE = "XXXXMC";
    private static final String EXISTING_COURT_CODE = "YORKCC";

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setup() {
        super.setup();
        jdbcTemplate.execute(String.format("DELETE FROM COURT WHERE CODE = '%s'", NEW_COURT_CODE));
    }

    @Nested
    class Update {
        @Test
        @DisplayName("Will reject request without correct role")
        void willRejectRequestWithoutCorrectRole() {
            final var token = createJwt("ROLE_COMMUNITY");

            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(updateRequest())
                .when()
                .put(String.format("court/code/%s", EXISTING_COURT_CODE))
                .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("Will reject request without the correct scope")
        void willRejectRequestWithoutTheCorrectScope() {
            final var token = createJwtWithScopes(List.of("read"), "ROLE_MAINTAIN_REF_DATA");

            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(updateRequest())
                .when()
                .put(String.format("court/code/%s", EXISTING_COURT_CODE))
                .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("Will reject request without court name")
        void willRejectRequestWithoutCourtName() {
            final var token = createJwtWithScopes(List.of("write"), "ROLE_MAINTAIN_REF_DATA");

            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(writeValueAsString(validUpdateRequest().toBuilder().courtName(null).build()))
                .when()
                .put(String.format("court/code/%s", EXISTING_COURT_CODE))
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Will reject request without court type")
        void willRejectRequestWithoutCourtType() {
            final var token = createJwtWithScopes(List.of("write"), "ROLE_MAINTAIN_REF_DATA");

            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(writeValueAsString(validUpdateRequest().toBuilder().courtTypeCode(null).build()))
                .when()
                .put(String.format("court/code/%s", EXISTING_COURT_CODE))
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Will accept valid request")
        void willAcceptAValidRequest() {
            final var token = createJwtWithScopes(List.of("write"), "ROLE_MAINTAIN_REF_DATA");

            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(validUpdateRequest())
                .when()
                .put(String.format("court/code/%s", EXISTING_COURT_CODE))
                .then()
                .statusCode(200);
        }

        private String updateRequest() {
            return writeValueAsString(validUpdateRequest());
        }

        private UpdateCourtDto validUpdateRequest() {
            return UpdateCourtDto
                .builder()
                .courtName("Sheffield New Court")
                .courtTypeCode("CRN")
                .build();
        }


    }

    @Nested
    class Insert {
        @Test
        @DisplayName("Will reject request without correct role")
        void willRejectRequestWithoutCorrectRole() {
            final var token = createJwt("ROLE_COMMUNITY");

            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(updateRequest())
                .when()
                .post("court")
                .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("Will reject request without the correct scope")
        void willRejectRequestWithoutTheCorrectScope() {
            final var token = createJwtWithScopes(List.of("read"), "ROLE_MAINTAIN_REF_DATA");

            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(updateRequest())
                .when()
                .post("court")
                .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("Will reject request without court name")
        void willRejectRequestWithoutCourtName() {
            final var token = createJwtWithScopes(List.of("write"), "ROLE_MAINTAIN_REF_DATA");

            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(writeValueAsString(insertRequest(null, NEW_COURT_CODE, "MAG", "N53")))
                .when()
                .post("court")
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Will reject request without court type")
        void willRejectRequestWithoutCourtType() {
            final var token = createJwtWithScopes(List.of("write"), "ROLE_MAINTAIN_REF_DATA");

            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(writeValueAsString(insertRequest("New Magistrates Court", NEW_COURT_CODE, null, "N53")))
                .when()
                .post("court")
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Will reject request without court code")
        void willRejectRequestWithoutCourtCode() {
            final var token = createJwtWithScopes(List.of("write"), "ROLE_MAINTAIN_REF_DATA");

            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(writeValueAsString(insertRequest("New Magistrates Court", null, "MAG", "N53")))
                .when()
                .post("court")
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Will reject request without probation area code")
        void willRejectRequestWithoutProbationAreaCode() {
            final var token = createJwtWithScopes(List.of("write"), "ROLE_MAINTAIN_REF_DATA");

            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(writeValueAsString(insertRequest("New Magistrates Court", NEW_COURT_CODE, "MAG", null)))
                .when()
                .post("court")
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Will accept valid request")
        void willAcceptAValidRequest() {
            final var token = createJwtWithScopes(List.of("write"), "ROLE_MAINTAIN_REF_DATA");

            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(validInsertRequest())
                .when()
                .post("court")
                .then()
                .statusCode(200);
        }

        private String updateRequest() {
            return writeValueAsString(validInsertRequest());
        }

        private NewCourtDto validInsertRequest() {
            return insertRequest("New Magistrates Court", NEW_COURT_CODE, "MAG", "N53");
        }
        private NewCourtDto insertRequest(String courtName, String code, String courtTypeCode, String probationArea) {
            return new NewCourtDto(code, courtTypeCode, true, courtName, null, null, "Crown Square", "High Street", "Town Centre", "Sheffield", "South Yorkshire", "S1 2BJ", "England", probationArea);
        }
    }

    @Nested
    class Get {
        @Test
        @DisplayName("Will reject request without correct role")
        void willRejectRequestWithoutCorrectRole() {
            final var token = createJwt("ROLE_COMMUNITY");

            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .when()
                .get(String.format("court/code/%s", EXISTING_COURT_CODE))
                .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("Will reject request without the correct scope")
        void willRejectRequestWithoutTheCorrectScope() {
            final var token = createJwtWithScopes(List.of("write"), "ROLE_MAINTAIN_REF_DATA");

            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .when()
                .get(String.format("court/code/%s", EXISTING_COURT_CODE))
                .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("Will accept valid request")
        void willAcceptAValidRequest() {
            final var token = createJwtWithScopes(List.of("read"), "ROLE_MAINTAIN_REF_DATA");

            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .when()
                .get(String.format("court/code/%s", EXISTING_COURT_CODE))
                .then()
                .statusCode(200);
        }
    }
}
