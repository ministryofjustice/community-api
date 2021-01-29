package uk.gov.justice.digital.delius.controller.secure;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.gov.justice.digital.delius.data.api.MappaDetails;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.ProbationArea;
import uk.gov.justice.digital.delius.data.api.StaffHuman;
import uk.gov.justice.digital.delius.data.api.Team;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.RiskService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class RiskAPITest extends IntegrationTestBase {

    @SpyBean
    private OffenderService offenderService;

    @SpyBean
    private RiskService riskService;

    @Nested
    @TestInstance(PER_CLASS)
    class SecureEndpoints {

        private Stream<Arguments> secureEndpoints() {
            return Stream.of(
                Arguments.of("/offenders/nomsNumber/NOMS/risk/mappa"),
                Arguments.of("/offenders/crn/CRN/risk/mappa")
            );
        }

        @ParameterizedTest
        @MethodSource("secureEndpoints")
        void noToken_unauthorised(String uri) {
            given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(uri)
                .then()
                .statusCode(401);
        }

        @ParameterizedTest
        @MethodSource("secureEndpoints")
        void missingRole_forbidden(String uri) {
            given()
                .auth().oauth2(createJwt())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(uri)
                .then()
                .statusCode(403);
        }

        @ParameterizedTest
        @MethodSource("secureEndpoints")
        void noOffender_notFound(String uri) {
            given()
                .auth().oauth2(createJwt("ROLE_COMMUNITY"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(uri)
                .then()
                .statusCode(404);
        }
    }

    @Nested
    class OffenderFound {

        @Test
        void nomsNumberFound_ok() {
            when(offenderService.offenderIdOfNomsNumber("NOMS")).thenReturn(Optional.of(1L));
            when(riskService.getMappaDetails(1L)).thenReturn(someMappaDetails());

            given()
                .auth().oauth2(createJwt("ROLE_COMMUNITY"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/NOMS/risk/mappa")
                .then()
                .statusCode(200);
        }

        @Test
        void crnFound_ok() {
            when(offenderService.offenderIdOfCrn("CRN")).thenReturn(Optional.of(1L));
            when(riskService.getMappaDetails(1L)).thenReturn(someMappaDetails());

            given()
                .auth().oauth2(createJwt("ROLE_COMMUNITY"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/CRN/risk/mappa")
                .then()
                .statusCode(200);
        }

        @Test
        void mappaDetailsReturned() {
            when(offenderService.offenderIdOfNomsNumber("NOMS")).thenReturn(Optional.of(1L));
            when(riskService.getMappaDetails(1L)).thenReturn(someMappaDetails());

            given()
                .auth().oauth2(createJwt("ROLE_COMMUNITY"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/NOMS/risk/mappa")
                .then()
                .statusCode(200)
                .body("level", equalTo(1))
                .body("category", equalTo(3))
                .body("startDate", equalTo(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .body("reviewDate", equalTo(LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .body("team.code", equalTo("SOME_TEAM"))
                .body("officer.code", equalTo("SOME_OFFICER"))
                .body("probationArea.code", equalTo("SOME_PROBATION_AREA"));
        }

        @NotNull
        private MappaDetails someMappaDetails() {
            return new MappaDetails(1, 3, LocalDate.now(), LocalDate.now().plusDays(1L), Team.builder().code("SOME_TEAM").build(), StaffHuman.builder().code("SOME_OFFICER").build(), ProbationArea.builder().code("SOME_PROBATION_AREA").build());
        }
    }
}
