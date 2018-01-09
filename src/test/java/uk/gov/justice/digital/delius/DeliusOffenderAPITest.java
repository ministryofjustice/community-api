package uk.gov.justice.digital.delius;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.DocumentMeta;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.jpa.entity.Offender;
import uk.gov.justice.digital.delius.jpa.entity.OffenderAddress;
import uk.gov.justice.digital.delius.jpa.entity.OffenderAlias;
import uk.gov.justice.digital.delius.jpa.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.user.UserData;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class DeliusOffenderAPITest {

    @LocalServerPort
    int port;

    @MockBean
    private OffenderRepository offenderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Jwt jwt;

    private WireMockServer wiremockServer;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));
        Mockito.when(offenderRepository.findByOffenderId(any(Long.class))).thenReturn(Optional.empty());
        wiremockServer = new WireMockServer(8088);
        wiremockServer.start();
    }

    @After
    public void teardown() {
        wiremockServer.stop();
    }

    @Test
    public void lookupUnknownOffenderIdGivesNotFound() {
        given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/offenderId/987654321")
                .then()
                .statusCode(404);
    }

    @Test
    public void lookupKnownOffenderNomsNumberGivesBasicOffender() {

        Mockito.when(offenderRepository.findByNomsNumber(eq("A12345"))).thenReturn(Optional.of(anOffender()));

        OffenderDetail offenderDetail =
                given()
                        .header("Authorization", aValidToken())
                        .when()
                        .get("/offenders/nomsNumber/A12345")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(OffenderDetail.class);

        assertThat(offenderDetail.getSurname()).isEqualTo("Sykes");
    }

    @Test
    public void lookupKnownOffenderCrnGivesBasicOffender() {

        Mockito.when(offenderRepository.findByCrn(eq("CRN123"))).thenReturn(Optional.of(anOffender()));

        OffenderDetail offenderDetail =
                given()
                        .header("Authorization", aValidToken())
                        .when()
                        .get("/offenders/crn/CRN123")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(OffenderDetail.class);

        assertThat(offenderDetail.getSurname()).isEqualTo("Sykes");
    }

    @Test
    public void lookupKnownOffenderIdGivesBasicOffender() {

        Mockito.when(offenderRepository.findByOffenderId(eq(1L))).thenReturn(Optional.of(anOffender()));

        OffenderDetail offenderDetail =
                given()
                        .header("Authorization", aValidToken())
                        .when()
                        .get("/offenders/offenderId/1")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(OffenderDetail.class);

        assertThat(offenderDetail.getSurname()).isEqualTo("Sykes");
        assertThat(offenderDetail.getOffenderAliases()).isEqualTo(Optional.empty());
        assertThat(offenderDetail.getContactDetails().getAddresses()).isEqualTo(Optional.empty());
    }

    @Test
    public void lookupKnownOffenderIdDetailGivesFullFatOffender() {

        Mockito.when(offenderRepository.findByOffenderId(eq(1L))).thenReturn(Optional.of(anOffender()));

        OffenderDetail offenderDetail =
                given()
                        .header("Authorization", aValidToken())
                        .when()
                        .get("/offenders/offenderId/1/all")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(OffenderDetail.class);

        assertThat(offenderDetail.getSurname()).isEqualTo("Sykes");
        assertThat(offenderDetail.getOffenderAliases().isPresent()).isTrue();
        assertThat(offenderDetail.getOffenderAliases().get()).isNotEmpty();
        assertThat(offenderDetail.getContactDetails().getAddresses().isPresent()).isTrue();
        assertThat(offenderDetail.getContactDetails().getAddresses().get()).isNotEmpty();
    }

    @Test
    public void lookupKnownOffenderNomsNumberDetailGivesFullFatOffender() {

        Mockito.when(offenderRepository.findByNomsNumber(eq("A12345"))).thenReturn(Optional.of(anOffender()));

        OffenderDetail offenderDetail =
                given()
                        .header("Authorization", aValidToken())
                        .when()
                        .get("/offenders/nomsNumber/A12345/all")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(OffenderDetail.class);

        assertThat(offenderDetail.getSurname()).isEqualTo("Sykes");
        assertThat(offenderDetail.getContactDetails().getAddresses().isPresent()).isTrue();
    }

    @Test
    public void lookupKnownOffenderCRNDetailGivesFullFatOffender() {

        Mockito.when(offenderRepository.findByCrn(eq("CRN123"))).thenReturn(Optional.of(anOffender()));

        OffenderDetail offenderDetail =
                given()
                        .header("Authorization", aValidToken())
                        .when()
                        .get("/offenders/crn/CRN123/all")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(OffenderDetail.class);

        assertThat(offenderDetail.getSurname()).isEqualTo("Sykes");
        assertThat(offenderDetail.getContactDetails().getAddresses().isPresent()).isTrue();
    }

    private Offender anOffender() {
        return Offender.builder()
                .allowSMS("Y")
                .crn("crn123")
                .croNumber("cro123")
                .currentDisposal(1L)
                .currentExclusion(2L)
                .currentHighestRiskColour("AMBER")
                .currentRemandStatus("ON_REMAND")
                .currentRestriction(3L)
                .dateOfBirthDate(LocalDate.of(1970, 1, 1))
                .emailAddress("bill@sykes.com")
                .establishment('A')
                .ethnicity(StandardReference.builder().codeDescription("IC1").build())
                .exclusionMessage("exclusion message")
                .firstName("Bill")
                .gender(StandardReference.builder().codeDescription("M").build())
                .immigrationNumber("IM123")
                .immigrationStatus(StandardReference.builder().codeDescription("N/A").build())
                .institutionId(4L)
                .interpreterRequired("N")
                .language(StandardReference.builder().codeDescription("ENGLISH").build())
                .languageConcerns("None")
                .mobileNumber("0718118055")
                .nationality(StandardReference.builder().codeDescription("BRITISH").build())
                .mostRecentPrisonerNumber("PN123")
                .niNumber("NI1234567")
                .nomsNumber("NOMS1234")
                .offenderId(5L)
                .pendingTransfer(6L)
                .pncNumber("PNC1234")
                .previousSurname("Jones")
                .religion(StandardReference.builder().codeDescription("COFE").build())
                .restrictionMessage("Restriction message")
                .secondName("Arthur")
                .surname("Sykes")
                .telephoneNumber("018118055")
                .title(StandardReference.builder().codeDescription("Mr").build())
                .secondNationality(StandardReference.builder().codeDescription("EIRE").build())
                .sexualOrientation(StandardReference.builder().codeDescription("STR").build())
                .previousConvictionDate(LocalDate.of(2016, 1, 1))
                .prevConvictionDocumentName("CONV1234")
                .offenderAliases(Lists.newArrayList(OffenderAlias.builder().build()))
                .offenderAddresses(Lists.newArrayList(OffenderAddress.builder().build()))
                .build();
    }

    @Test
    public void cannotGetOffenderByOffenderIdWithoutJwtAuthorizationHeader() {
        when()
                .get("/offenders/offenderId/1")
                .then()
                .statusCode(401);
    }

    @Test
    public void cannotGetOffenderByOffenderIdWithSomeoneElsesJwtAuthorizationHeader() {
        given()
                .header("Authorization", someoneElsesToken())
                .when()
                .get("/offenders/offenderId/1")
                .then()
                .statusCode(401);
    }

    @Test
    public void cannotGetOffenderByOffenderIdWithJunkJwtAuthorizationHeader() {
        given()
                .header("Authorization", UUID.randomUUID().toString())
                .when()
                .get("/offenders/offenderId/1")
                .then()
                .statusCode(401);
    }

    private String someoneElsesToken() {
        return "Bearer " + new Jwt("Someone elses secret", 1).buildToken(UserData.builder().distinguishedName(UUID.randomUUID().toString()).build());
    }

    private String aValidToken() {
        return "Bearer " + jwt.buildToken(UserData.builder().distinguishedName(UUID.randomUUID().toString()).build());
    }

    @Test
    public void canListOffenderDocumentsByOffenderCRN() {
        DocumentMeta[] documentList = given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/crn/crn123/documents")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(DocumentMeta[].class);

        DocumentMeta expectedDoc = aDocumentMeta();

        assertThat(Arrays.asList(documentList)).containsOnly(expectedDoc);
    }

    @Test
    public void canListOffenderDocumentsByOffenderId() {
        Mockito.when(offenderRepository.findByOffenderId(eq(1L))).thenReturn(Optional.of(anOffender()));


        DocumentMeta[] documentList = given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/offenderId/1/documents")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(DocumentMeta[].class);

        DocumentMeta expectedDoc = aDocumentMeta();

        assertThat(Arrays.asList(documentList)).containsOnly(expectedDoc);
    }

    @Test
    public void canListOffenderDocumentsByNomsNumber() {
        Mockito.when(offenderRepository.findByNomsNumber(eq("A12345"))).thenReturn(Optional.of(anOffender()));

        DocumentMeta[] documentList = given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/nomsNumber/A12345/documents")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(DocumentMeta[].class);

        DocumentMeta expectedDoc = aDocumentMeta();

        assertThat(Arrays.asList(documentList)).containsOnly(expectedDoc);
    }

    private DocumentMeta aDocumentMeta() {
        return DocumentMeta.builder()
                .docType("DOCUMENT")
                .entityType("CONTACT")
                .createdAt(OffsetDateTime.parse("2018-01-03T13:20:35Z"))
                .lastModifiedAt(OffsetDateTime.parse("2018-01-03T13:20:35Z"))
                .id("fa63c379-8b31-4e36-a152-2a57dfe251c4")
                .documentName("TS2 Trg Template Letter_03012018_132035_Pickett_K_D002384.DOC")
                .build();
    }

    @Test
    public void listOffenderDocumentsForUnknownOffenderCRNGives404() {
        given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/crn/D002385/documents")
                .then()
                .statusCode(404);
    }

    @Test
    public void canGetOffenderDocumentDetailsByOffenderCrnAndDocumentId() {
        DocumentMeta documentMeta = given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/crn/crn123/documents/fa63c379-8b31-4e36-a152-2a57dfe251c4/detail")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(DocumentMeta.class);

        DocumentMeta expectedDoc = aDocumentMeta();

        assertThat(documentMeta).isEqualTo(expectedDoc);
    }

    @Test
    public void canGetOffenderDocumentDetailsByOffenderIdAndDocumentId() {
        Mockito.when(offenderRepository.findByOffenderId(eq(1L))).thenReturn(Optional.of(anOffender()));


        DocumentMeta documentMeta = given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/offenderId/1/documents/fa63c379-8b31-4e36-a152-2a57dfe251c4/detail")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(DocumentMeta.class);

        DocumentMeta expectedDoc = aDocumentMeta();

        assertThat(documentMeta).isEqualTo(expectedDoc);
    }

    @Test
    public void canGetOffenderDocumentDetailsByOffenderNomsNumberAndDocumentId() {
        Mockito.when(offenderRepository.findByNomsNumber(eq("A12345"))).thenReturn(Optional.of(anOffender()));


        DocumentMeta documentMeta = given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/nomsNumber/A12345/documents/fa63c379-8b31-4e36-a152-2a57dfe251c4/detail")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(DocumentMeta.class);

        DocumentMeta expectedDoc = aDocumentMeta();

        assertThat(documentMeta).isEqualTo(expectedDoc);
    }

    @Test
    public void getOffenderDocumentDetailsForUnknownOffenderCRNGives404() {
        given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/crn/D002385/documents/fa63c379-8b31-4e36-a152-2a57dfe251c4/detail")
                .then()
                .statusCode(404);
    }

    @Test
    public void getOffenderDocumentDetailsForKnownOffenderCRNButUnknownDocumentGives404() {
        given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/crn/D002384/documents/" + UUID.randomUUID().toString() + "/detail")
                .then()
                .statusCode(404);
    }

    @Test
    public void canRetrieveOffenderDocument() {

        given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/crn/crn123/documents/fa63c379-8b31-4e36-a152-2a57dfe251c4")
                .then()
                .header("Content-Disposition", "attachment; filename=\"TS2 Trg Template Letter_03012018_132035_Pickett_K_D002384.DOC\"")
                .statusCode(200);
    }

    @Test
    public void retrieveDocumentForUnknownOffenderCRNGives404() {
        given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/crn/D002385/documents/fa63c379-8b31-4e36-a152-2a57dfe251c4")
                .then()
                .statusCode(404);
    }

    @Test
    public void retrieveDocumentForKnownOffenderButUnknownDocumentGives404() {
        given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/crn/D002384/documents/" + UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }


}