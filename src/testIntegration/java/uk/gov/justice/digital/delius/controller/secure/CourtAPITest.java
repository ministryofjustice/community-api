package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.digital.delius.data.api.NewCourtDto;
import uk.gov.justice.digital.delius.data.api.UpdateCourtDto;
import uk.gov.justice.digital.delius.jpa.national.repository.UserRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

record CourtId(String code, long id) {
}

@ExtendWith({SpringExtension.class})
public class CourtAPITest extends IntegrationTestBase {
    private static final String NEW_COURT_CODE = "XXXXMC";
    private static final CourtId EXISTING_COURT_CODE = new CourtId("SHEFCC", 1500004904);


    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    public void removeAnyCreatedCourts() {
        jdbcTemplate.execute(String.format("DELETE FROM COURT WHERE CODE = '%s'", NEW_COURT_CODE));
        jdbcTemplate.update("UPDATE COURT SET BUILDING_NAME = ? WHERE COURT_ID = ?", "Sheffield Combined Crt Centre", EXISTING_COURT_CODE
            .id());
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
                .put(String.format("courts/code/%s", EXISTING_COURT_CODE.code()))
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
                .put(String.format("courts/code/%s", EXISTING_COURT_CODE.code()))
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
                .put(String.format("courts/code/%s", EXISTING_COURT_CODE.code()))
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
                .put(String.format("courts/code/%s", EXISTING_COURT_CODE.code()))
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Will accept valid request")
        void willAcceptAValidRequest() {
            final var token = createJwtWithScopes(List.of("write", "read"), "ROLE_MAINTAIN_REF_DATA");

            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(updateRequest())
                .when()
                .put(String.format("courts/code/%s", EXISTING_COURT_CODE.code()))
                .then()
                .statusCode(200);
        }

        @Test
        @DisplayName("Will update court along with the last updated details")
        void willUpdateCourtAndRecordWhenItWasUpdated() {
            final var expectedUpdateUserId = userRepository
                .findByDistinguishedNameIgnoreCase("APIUser")
                .orElseThrow()
                .getUserId();
            final var courtEntity = courtRepository.findById(EXISTING_COURT_CODE.id()).orElseThrow();
            final var token = createJwtWithScopes(List.of("write", "read"), "ROLE_MAINTAIN_REF_DATA");

            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(updateRequest(courtEntity, "New Crown Building"))
                .when()
                .put(String.format("courts/code/%s", EXISTING_COURT_CODE.code()))
                .then()
                .statusCode(200);

            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .when()
                .get(String.format("courts/code/%s", EXISTING_COURT_CODE.code()))
                .then()
                .statusCode(200)
                .body("buildingName", equalTo("New Crown Building"));

            final var courtEntityAfterUpdate = courtRepository.findById(EXISTING_COURT_CODE.id()).orElseThrow();

            assertThat(courtEntityAfterUpdate.getLastUpdatedDatetime())
                .isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
            assertThat(courtEntityAfterUpdate.getLastUpdatedUserId()).isEqualTo(expectedUpdateUserId);
        }

        private String updateRequest(uk.gov.justice.digital.delius.jpa.standard.entity.Court court, @SuppressWarnings("SameParameterValue") String buildingName) {
            return writeValueAsString(validUpdateRequest(court).toBuilder().buildingName(buildingName).build());
        }

        private String updateRequest() {
            return writeValueAsString(validUpdateRequest());
        }

        private UpdateCourtDto validUpdateRequest() {
            final var courtEntity = courtRepository.findById(EXISTING_COURT_CODE.id()).orElseThrow();
            return validUpdateRequest(courtEntity);
        }

        private UpdateCourtDto validUpdateRequest(uk.gov.justice.digital.delius.jpa.standard.entity.Court court) {
            return UpdateCourtDto
                .builder()
                .active(court.getSelectable().equals("Y"))
                .buildingName(court.getBuildingName())
                .courtName(court.getCourtName())
                .courtTypeCode(court.getCourtType().getCodeValue())
                .country(court.getCountry())
                .county(court.getCounty())
                .fax(court.getFax())
                .locality(court.getLocality())
                .postcode(court.getPostcode())
                .street(court.getStreet())
                .telephoneNumber(court.getTelephoneNumber())
                .town(court.getTown())
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
                .body(validInsertRequest())
                .when()
                .post("courts")
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
                .body(validInsertRequest())
                .when()
                .post("courts")
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
                .body(insertRequest(null, NEW_COURT_CODE, "MAG", "N53"))
                .when()
                .post("courts")
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
                .body(insertRequest("New Magistrates Court", NEW_COURT_CODE, null, "N53"))
                .when()
                .post("courts")
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
                .body(insertRequest("New Magistrates Court", null, "MAG", "N53"))
                .when()
                .post("courts")
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
                .body(insertRequest("New Magistrates Court", NEW_COURT_CODE, "MAG", null))
                .when()
                .post("courts")
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Will accept valid request and insert new court record the user that created the court")
        void willAcceptAValidRequest() {
            final var expectedUpdateUserId = userRepository
                .findByDistinguishedNameIgnoreCase("APIUser")
                .orElseThrow()
                .getUserId();

            final var token = createJwtWithScopes(List.of("write", "read"), "ROLE_MAINTAIN_REF_DATA");

            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(validInsertRequest())
                .when()
                .post("courts")
                .then()
                .statusCode(200)
                .body("code", equalTo(NEW_COURT_CODE));

            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .when()
                .get(String.format("courts/code/%s", NEW_COURT_CODE))
                .then()
                .statusCode(200)
                .body("code", equalTo(NEW_COURT_CODE));

            final var courtEntityAfterUpdate = courtRepository
                .findByCode(NEW_COURT_CODE)
                .stream()
                .findFirst()
                .orElseThrow();

            assertThat(courtEntityAfterUpdate.getLastUpdatedDatetime())
                .isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
            assertThat(courtEntityAfterUpdate.getLastUpdatedUserId()).isEqualTo(expectedUpdateUserId);
            assertThat(courtEntityAfterUpdate.getCreatedDatetime())
                .isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
            assertThat(courtEntityAfterUpdate.getCreatedByUserId()).isEqualTo(expectedUpdateUserId);

        }

        private String validInsertRequest() {
            return insertRequest("New Magistrates Court", NEW_COURT_CODE, "MAG", "N02");
        }

        private String insertRequest(String courtName, String code, String courtTypeCode, String probationArea) {
            return writeValueAsString(new NewCourtDto(code, courtTypeCode, true, courtName, "0114 555 1234", "0114 555 4321", "Crown Square", "High Street", "Town Centre", "Sheffield", "South Yorkshire", "S1 2BJ", "England", probationArea));
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
                .get(String.format("courts/code/%s", EXISTING_COURT_CODE.code()))
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
                .get(String.format("courts/code/%s", EXISTING_COURT_CODE.code()))
                .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("Will accept valid request and return court details")
        void willAcceptAValidRequest() {
            final var token = createJwtWithScopes(List.of("read"), "ROLE_MAINTAIN_REF_DATA");

            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .when()
                .get(String.format("courts/code/%s", EXISTING_COURT_CODE.code()))
                .then()
                .statusCode(200)
                .body("code", equalTo(EXISTING_COURT_CODE.code()))
                .body("selectable", equalTo(true))
                .body("courtName", equalTo("Sheffield Crown Court"));
        }
    }

    @Nested
    class GetAll {
        @Test
        @DisplayName("Will reject request without correct role")
        void willRejectRequestWithoutCorrectRole() {
            final var token = createJwt("ROLE_COMMUNITY");

            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .when()
                .get("courts")
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
                .get("courts")
                .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("Will accept valid request and return court details")
        void willAcceptAValidRequest() {
            final var token = createJwtWithScopes(List.of("read"), "ROLE_MAINTAIN_REF_DATA");

            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .when()
                .get("courts")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(1))
                .root(String.format("find { it.code == '%s' }", EXISTING_COURT_CODE.code()))
                .body("code", equalTo(EXISTING_COURT_CODE.code()))
                .body("selectable", equalTo(true))
                .body("courtName", equalTo("Sheffield Crown Court"));
        }
    }
}
