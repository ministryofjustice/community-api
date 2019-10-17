package uk.gov.justice.digital.delius;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.ConvictionDocuments;
import uk.gov.justice.digital.delius.data.api.OffenderDocumentDetail;
import uk.gov.justice.digital.delius.data.api.OffenderDocuments;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.user.UserData;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev-seed")
@DirtiesContext
public class OffenderDocumentsAPITest {

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Jwt jwt;

    @Before
    public void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));
    }

    @Test
    public void canGetOffenderDocumentsByCrn() {

        OffenderDocuments offenderDocuments = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/crn/X320741/documents/grouped")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(OffenderDocuments.class);

        assertThat(toDocumentIds(offenderDocuments))
                .hasSize(22)
                .containsExactlyInAnyOrder(
                        "95fbd516-e2ab-4193-8727-a5bac9cf6a14",
                        "ec450eca-cf81-420d-8712-873a5df2274b",
                        "086bfb96-28a7-4b0a-80d5-7b877dd7bb75",
                        "5058ca66-3751-4701-855a-86bf518d9392",
                        "1d842fce-ec2d-45dc-ac9a-748d3076ca6b",
                        "e7d09bf6-b2e2-44eb-82be-2f561b575f1a",
                        "424d3c35-12c5-41cf-8f0a-2e2eea46f45d",
                        "e6cf2802-32d5-4a91-a7a9-00064d27bb19",
                        "0ec9b16c-b292-4d27-b11a-c0ddde852804",
                        "aeb43e06-a4a1-460f-9acf-e2495de84604",
                        "b2d92238-215c-4d6d-b91c-208ea747087e",
                        "f4d0fec8-c0bf-4297-9067-a5b410b69157",
                        "b88547cf-7464-4cbc-b5f9-ebe2bafc19d9",
                        "bc1fd68e-9882-4c4e-8aef-7df4452830fc",
                        "5152b060-9650-4f22-9974-038a38590d9f",
                        "4191eada-6e03-4bd8-b52d-9e050eefa745",
                        "7ceda384-3624-4a62-849d-7c729b6d0dd1",
                        "44f37749-18b8-46ff-803a-150746f6d1bc",
                        "2b167448-31a5-45a5-85a5-dcdd4a783f1d",
                        "1e593ff6-d5d6-4048-a671-cdeb8f65608b",  // pre-cons from offender
                        "cc8bf04c-2f8c-4e72-a14b-ab6a5702bf59",  // cps pack from event
                        "04897043-6b84-45b8-b278-4fffea477ef3"   // cps pack from event
                )
                .doesNotContain(
                        "c25c9ed5-e78c-4f5f-aca6-4773eb1e3c7c"  // orphaned document from missing contact
                );
    }

    @Test
    public void canGetOffenderDocumentsByNomsNumber() {

        OffenderDocuments offenderDocuments = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/nomsNumber/G9542VP/documents/grouped")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(OffenderDocuments.class);

        assertThat(toDocumentIds(offenderDocuments))
                .hasSize(22);
    }

    @Test
    public void canGetOffenderDocumentsByOffenderId() {

        OffenderDocuments offenderDocuments = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/offenderId/2500343964/documents/grouped")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(OffenderDocuments.class);

        assertThat(toDocumentIds(offenderDocuments))
                .hasSize(22);
    }

    @Test
    public void getOffenderDocumentsForUnknownCrnReturnsNotFound() {
        given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/crn/notFoundCrn/documents/grouped")
            .then()
            .statusCode(404);
    }

    @Test
    public void getOffenderDocumentsForUnknownNomsNumberReturnsNotFound() {
        given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/nomsNumber/notFoundNomsNumber/documents/grouped")
            .then()
            .statusCode(404);
    }

    @Test
    public void getOffenderDocumentsForUnknownOffenderIdReturnsNotFound() {
        given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/offenderId/99/documents/grouped")
            .then()
            .statusCode(404);
    }

    @Test
    public void getOffenderDocumentsByCrnMustHaveValidJwt() {
        given()
            .when()
            .get("offenders/crn/X320741/documents/grouped")
            .then()
            .statusCode(401);
    }

    private String aValidToken() {
        return aValidTokenFor(UUID.randomUUID().toString());
    }

    private String aValidTokenFor(String distinguishedName) {
        return "Bearer " + jwt.buildToken(UserData.builder()
                .distinguishedName(distinguishedName)
                .uid("bobby.davro").build());
    }

    private List<String> toDocumentIds(OffenderDocuments offenderDocuments) {
        return ImmutableList.<OffenderDocumentDetail>builder()
                .addAll(offenderDocuments.getDocuments())
                .addAll(offenderDocuments
                        .getConvictions()
                        .stream()
                        .map(ConvictionDocuments::getDocuments)
                        .flatMap(List::stream)
                        .collect(toList()))
                .build()
                .stream()
                .map(OffenderDocumentDetail::getId)
                .collect(toList());
    }
}
