package uk.gov.justice.digital.delius;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.AccessLimitation;
import uk.gov.justice.digital.delius.data.api.Address;
import uk.gov.justice.digital.delius.data.api.Count;
import uk.gov.justice.digital.delius.data.api.DocumentMeta;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.OffenderDetailSummary;
import uk.gov.justice.digital.delius.jpa.national.entity.Exclusion;
import uk.gov.justice.digital.delius.jpa.national.entity.Restriction;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.jpa.national.repository.UserRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderAddress;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.user.UserData;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static uk.gov.justice.digital.delius.util.OffenderHelper.anOffender;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"offender.ids.pagesize=5"})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class DeliusOffenderAPITest {

    @LocalServerPort
    int port;

    @MockBean
    private OffenderRepository offenderRepository;

    @MockBean
    private UserRepository userRepository;

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
        Mockito.when(offenderRepository.listOffenderIds(eq(1), eq(5))).thenReturn(LongStream.rangeClosed(1, 5).mapToObj(BigDecimal::valueOf).collect(Collectors.toList()));
        Mockito.when(offenderRepository.listOffenderIds(eq(6), eq(10))).thenReturn(LongStream.rangeClosed(6, 10).mapToObj(BigDecimal::valueOf).collect(Collectors.toList()));
        Mockito.when(offenderRepository.count()).thenReturn(666L);
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

        OffenderDetailSummary offenderDetail =
                given()
                        .header("Authorization", aValidToken())
                        .when()
                        .get("/offenders/nomsNumber/A12345")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(OffenderDetailSummary.class);

        assertThat(offenderDetail.getSurname()).isEqualTo("Sykes");
    }

    @Test
    public void lookupKnownOffenderCrnGivesBasicOffender() {

        Mockito.when(offenderRepository.findByCrn(eq("CRN123"))).thenReturn(Optional.of(anOffender()));

        OffenderDetailSummary offenderDetail =
                given()
                        .header("Authorization", aValidToken())
                        .when()
                        .get("/offenders/crn/CRN123")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(OffenderDetailSummary.class);

        assertThat(offenderDetail.getSurname()).isEqualTo("Sykes");
    }

    @Test
    public void lookupKnownOffenderIdGivesBasicOffender() {

        Mockito.when(offenderRepository.findByOffenderId(eq(1L))).thenReturn(Optional.of(
                anOffender().toBuilder().currentExclusion(1L).currentRestriction(1L).build()));

        OffenderDetailSummary offenderDetail =
                given()
                        .header("Authorization", aValidToken())
                        .when()
                        .get("/offenders/offenderId/1")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(OffenderDetailSummary.class);

        assertThat(offenderDetail.getSurname()).isEqualTo("Sykes");
        assertThat(offenderDetail.getCurrentExclusion()).isTrue();
        assertThat(offenderDetail.getCurrentRestriction()).isTrue();
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
        assertThat(offenderDetail.getOffenderAliases()).isNotEmpty();
        assertThat(offenderDetail.getContactDetails().getAddresses()).isNotEmpty();
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
        assertThat(offenderDetail.getContactDetails().getAddresses()).isNotEmpty();
    }

    @Test
    public void lookupKnownOffenderCRNDetailGivesFullFatOffender() {

        OffenderAddress mainAddress = OffenderAddress.builder()
                .streetName("Foo Street")
                .addressStatus(StandardReference.builder()
                        .codeValue("M")
                        .codeDescription("Main address").build())
                .build();
        Offender offender = anOffender().toBuilder()
                .offenderAddresses(asList(mainAddress)).build();
        Mockito.when(offenderRepository.findByCrn(eq("CRN123"))).thenReturn(Optional.of(offender));

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
        assertThat(offenderDetail.getContactDetails().getAddresses()).isNotEmpty();

        Address address = offenderDetail.getContactDetails().getAddresses().get(0);
        assertThat(address.getStreetName()).isEqualTo("Foo Street");
        assertThat(address.getStatus().getCode()).isEqualTo("M");
        assertThat(address.getStatus().getDescription()).isEqualTo("Main address");
    }

    @Test
    public void lookupKnownOffenderCRNDetailGivesFullFatOffenderWithANullAddressStatus() {

        OffenderAddress mainAddress = OffenderAddress.builder()
                .streetName("Foo Street")
                .build();
        Offender offender = anOffender().toBuilder()
                .offenderAddresses(asList(mainAddress)).build();
        Mockito.when(offenderRepository.findByCrn(eq("CRN123"))).thenReturn(Optional.of(offender));

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
        assertThat(offenderDetail.getContactDetails().getAddresses()).isNotEmpty();

        Address address = offenderDetail.getContactDetails().getAddresses().get(0);
        assertThat(address.getStreetName()).isEqualTo("Foo Street");
        assertThat(address.getStatus()).isNull();
    }

    @Test
    public void cannotGetOffenderByOffenderIdWithoutJwtAuthorizationHeader() {
        when()
                .get("/offenders/offenderId/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void cannotGetOffenderByOffenderIdWithSomeoneElsesJwtAuthorizationHeader() {
        given()
                .header("Authorization", someoneElsesToken())
                .when()
                .get("/offenders/offenderId/1")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void cannotGetOffenderByOffenderIdWithJunkJwtAuthorizationHeader() {
        given()
                .header("Authorization", UUID.randomUUID().toString())
                .when()
                .get("/offenders/offenderId/1")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    private String someoneElsesToken() {
        return "Bearer " + new Jwt("Someone elses secret", 1).buildToken(UserData.builder().distinguishedName(UUID.randomUUID().toString()).build());
    }

    private String aValidToken() {
        return aValidTokenFor(UUID.randomUUID().toString());
    }

    private String aValidTokenFor(String distinguishedName) {
        return "Bearer " + jwt.buildToken(UserData.builder()
                .distinguishedName(distinguishedName)
                .uid("bobby.davro").build());
    }


    @Test
    public void canListOffenderDocumentsByOffenderCRN() throws IOException {
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
    public void canListOffenderDocumentsByOffenderId() throws IOException {
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
    public void canListOffenderDocumentsByNomsNumber() throws IOException {
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

    private DocumentMeta aDocumentMeta() throws IOException {
        return DocumentMeta.builder()
                .docType("DOCUMENT")
                .entityType("CONTACT")
                .createdAt(OffsetDateTime.parse("2018-01-03T13:20:35Z"))
                .lastModifiedAt(OffsetDateTime.parse("2018-01-03T13:20:35Z"))
                .id("fa63c379-8b31-4e36-a152-2a57dfe251c4")
                .documentName("TS2 Trg Template Letter_03012018_132035_Pickett_K_D002384.DOC")
                .author("NDelius02,NDelius02")
                .userData(objectMapper.readValue("{\"templateName\":\"shortFormatPreSentenceReport\",\"values\":{\"issueBehaviourDetails\":\"\",\"oasysAssessmentsInformationSource\":false,\"issueFinanceDetails\":\"\",\"pageNumber\":2,\"issueSubstanceMisuse\":false,\"office\":\"\",\"riskOfSeriousHarm\":\"\",\"counterSignature\":\"\",\"feedback\":\"Really great service. Saves me so much time\",\"jumpNumber\":2,\"issueOther\":false,\"reportAuthor\":\"\",\"reportDate\":\"08/12/2017\",\"additionalPreviousSupervision\":\"\",\"likelihoodOfReOffending\":\"\",\"otherOffences\":\"\",\"issueEmploymentDetails\":\"\",\"cpsSummaryInformationSource\":false,\"proposal\":\"\",\"watermark\":\"DRAFT\",\"pnc\":\"\",\"childrenServicesInformationSource\":false,\"court\":\"Mansfield  Magistrates Court\",\"issueSubstanceMisuseDetails\":\"\",\"mainOffence\":\"\",\"offenceSummary\":\"\",\"issueAccommodationDetails\":\"\",\"name\":\"Johnny PDF\",\"issueBehaviour\":false,\"interviewInformationSource\":false,\"startDate\":\"08/12/2017\",\"issueAccommodation\":false,\"issueRelationshipsDetails\":\"\",\"onBehalfOfUser\":\"andy.marke,andy.marke\",\"courtOfficePhoneNumber\":\"\",\"pncSupplied\":false,\"sentencingGuidelinesInformationSource\":false,\"otherInformationDetails\":\"\",\"issueFinance\":false,\"policeInformationSource\":false,\"otherInformationSource\":false,\"previousSupervisionResponse\":\"\",\"dateOfHearing\":\"27/07/2017\",\"previousConvictionsInformationSource\":false,\"crn\":\"X087946\",\"issueRelationships\":false,\"address\":\"123\\nFake St\\n\",\"issueEmployment\":false,\"localJusticeArea\":\"\",\"dateOfBirth\":\"26/07/1977\",\"entityId\":2500032066,\"serviceRecordsInformationSource\":false,\"visitedPages\":\"[1,2,10]\",\"addressSupplied\":true,\"patternOfOffending\":\"\",\"issueHealth\":false,\"offenceAnalysis\":\"\",\"issueHealthDetails\":\"\",\"issueOtherDetails\":\"\",\"documentId\":\"bc4796ce-09d6-4f04-b715-a3bc8173ac35\",\"age\":40,\"victimStatementInformationSource\":false}}", ObjectNode.class))
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
    public void canGetOffenderDocumentDetailsByOffenderCrnAndDocumentId() throws IOException {
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
    public void canGetOffenderDocumentDetailsByOffenderIdAndDocumentId() throws IOException {
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
    public void canGetOffenderDocumentDetailsByOffenderNomsNumberAndDocumentId() throws IOException {
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

    @Test
    public void canRetrieveOffenderIdsWithDefaultPageSizeAndPage() {

        Map<String, Object> ids = given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/offenderIds")
                .then()
                .statusCode(200)
                .extract().body().as(Map.class);

        List<Integer> offenderIds = (List<Integer>) ids.get("offenderIds");

        assertThat(offenderIds).containsExactly(
                Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5));
    }

    @Test
    public void canRetrieveOffenderIdsWithExplicitPageSizeAndPage() {

        Map<String, Object> ids = given()
                .header("Authorization", aValidToken())
                .when()
                .queryParams("pageSize", 5, "page", 2)
                .get("/offenders/offenderIds")
                .then()
                .statusCode(200)
                .extract().body().as(Map.class);

        List<Integer> offenderIds = (List<Integer>) ids.get("offenderIds");

        assertThat(offenderIds).containsExactly(
                Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(10));
    }

    @Test
    public void getOffenderIdsProvidesLinkToNextPage() {

        JsonNode ids = given()
                .header("Authorization", aValidToken())
                .when()
                .queryParams("pageSize", 5, "page", 1)
                .get("/offenders/offenderIds")
                .then()
                .statusCode(200)
                .extract().body().as(JsonNode.class);

        assertThat(ids.findPath("_links").findPath("next").findPath("href").asText()).endsWith("/api/offenders/offenderIds?pageSize=5&page=2");
    }

    @Test
    public void offenderCountProvidesCount() {
        Count count = given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/count")
                .then()
                .statusCode(200)
                .extract().body().as(Count.class);

        assertThat(count.getValue()).isEqualTo(666l);

    }

    @Test
    public void userAccessForOffenderIdWithNoAccessLimitationsReturnsAppropriate() {
        Mockito.when(offenderRepository.findByOffenderId(eq(1L))).thenReturn(Optional.of(anOffender()));

        AccessLimitation accessLimitation = given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/offenderId/1/userAccess")
                .then()
                .statusCode(200)
                .extract().body().as(AccessLimitation.class);

        assertThat(accessLimitation.isUserExcluded()).isFalse();
        assertThat(accessLimitation.isUserRestricted()).isFalse();
    }

    @Test
    public void userAccessForOffenderCrnWithNoAccessLimitationsReturnsAppropriate() {
        Mockito.when(offenderRepository.findByCrn(eq("123"))).thenReturn(Optional.of(anOffender()));

        AccessLimitation accessLimitation = given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/crn/123/userAccess")
                .then()
                .statusCode(200)
                .extract().body().as(AccessLimitation.class);

        assertThat(accessLimitation.isUserExcluded()).isFalse();
        assertThat(accessLimitation.isUserRestricted()).isFalse();
    }

    @Test
    public void userAccessForOffenderNomsNumberWithNoAccessLimitationsReturnsAppropriate() {
        Mockito.when(offenderRepository.findByNomsNumber(eq("NOMS123"))).thenReturn(Optional.of(anOffender()));

        AccessLimitation accessLimitation = given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/nomsNumber/NOMS123/userAccess")
                .then()
                .statusCode(200)
                .extract().body().as(AccessLimitation.class);

        assertThat(accessLimitation.isUserExcluded()).isFalse();
        assertThat(accessLimitation.isUserRestricted()).isFalse();
    }

    @Test
    public void userAccessForOffenderIdWithExclusionForUserReturnsAppropriate() {
        Offender offender = anOffender().toBuilder().currentExclusion(1L).build();
        Mockito.when(offenderRepository.findByOffenderId(eq(1L))).thenReturn(Optional.of(offender));
        String distinguishedName = UUID.randomUUID().toString();

        Mockito.when(userRepository.findByDistinguishedNameIgnoreCase("bobby.davro")).thenReturn(
                Optional.of(User.builder()
                        .exclusions(
                                Lists.newArrayList(Exclusion.builder().offenderId(offender.getOffenderId()).build())
                        )
                        .restrictions(Lists.emptyList())
                        .build()));

        AccessLimitation accessLimitation = given()
                .header("Authorization", aValidTokenFor(distinguishedName))
                .when()
                .get("/offenders/offenderId/1/userAccess")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .extract().body().as(AccessLimitation.class);

        assertThat(accessLimitation.isUserExcluded()).isTrue();
        assertThat(accessLimitation.isUserRestricted()).isFalse();
    }

    @Test
    public void userAccessForOffenderIdWithExclusionForSomeOtherUserReturnsAppropriate() {
        Offender offender = anOffender().toBuilder().currentExclusion(1L).build();
        Mockito.when(offenderRepository.findByOffenderId(eq(1L))).thenReturn(Optional.of(offender));
        String distinguishedName = UUID.randomUUID().toString();

        Mockito.when(userRepository.findByDistinguishedNameIgnoreCase("bobby.davro")).thenReturn(
                Optional.of(User.builder()
                        .restrictions(Lists.emptyList())
                        .exclusions(Lists.emptyList())
                        .build()));

        AccessLimitation accessLimitation = given()
                .header("Authorization", aValidTokenFor(distinguishedName))
                .when()
                .get("/offenders/offenderId/1/userAccess")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().body().as(AccessLimitation.class);

        assertThat(accessLimitation.isUserExcluded()).isFalse();
        assertThat(accessLimitation.isUserRestricted()).isFalse();
    }

    @Test
    public void userAccessForOffenderIdWithRestrictionIncludingUserReturnsAppropriate() {
        Offender offender = anOffender().toBuilder().currentRestriction(1L).build();
        Mockito.when(offenderRepository.findByOffenderId(eq(1L))).thenReturn(Optional.of(offender));
        String distinguishedName = UUID.randomUUID().toString();

        Mockito.when(userRepository.findByDistinguishedNameIgnoreCase("bobby.davro")).thenReturn(
                Optional.of(User.builder()
                        .restrictions(
                                Lists.newArrayList(Restriction.builder().offenderId(offender.getOffenderId()).build())
                        )
                        .exclusions(Lists.emptyList())
                        .build()));

        AccessLimitation accessLimitation = given()
                .header("Authorization", aValidTokenFor(distinguishedName))
                .when()
                .get("/offenders/offenderId/1/userAccess")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().body().as(AccessLimitation.class);

        assertThat(accessLimitation.isUserExcluded()).isFalse();
        assertThat(accessLimitation.isUserRestricted()).isFalse();
    }

    @Test
    public void userAccessForOffenderIdWithRestrictionExcludingUserReturnsAppropriate() {
        Offender offender = anOffender().toBuilder().currentRestriction(1L).build();
        Mockito.when(offenderRepository.findByOffenderId(eq(1L))).thenReturn(Optional.of(offender));
        String distinguishedName = UUID.randomUUID().toString();

        Mockito.when(userRepository.findByDistinguishedNameIgnoreCase("bobby.davro")).thenReturn(
                Optional.of(User.builder()
                        .restrictions(Lists.emptyList())
                        .exclusions(Lists.emptyList())
                        .build()));

        AccessLimitation accessLimitation = given()
                .header("Authorization", aValidTokenFor(distinguishedName))
                .when()
                .get("/offenders/offenderId/1/userAccess")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .extract().body().as(AccessLimitation.class);

        assertThat(accessLimitation.isUserExcluded()).isFalse();
        assertThat(accessLimitation.isUserRestricted()).isTrue();
    }

    @Test
    public void canGetOffenderManagersByOffenderNomsNumber() {
        Mockito.when(offenderRepository.findByNomsNumber(eq("A12345"))).thenReturn(Optional.of(anOffender()));


        uk.gov.justice.digital.delius.data.api.OffenderManager[] offenderManagers = given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/nomsNumber/A12345/offenderManagers")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(uk.gov.justice.digital.delius.data.api.OffenderManager[].class);

        assertThat(offenderManagers).hasSize(1);
    }

    @Test
    public void canGetOffenderManagersByOffenderId() {
        Mockito.when(offenderRepository.findByOffenderId(eq(1L))).thenReturn(Optional.of(anOffender()));


        uk.gov.justice.digital.delius.data.api.OffenderManager[] offenderManagers = given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/offenderId/1/offenderManagers")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(uk.gov.justice.digital.delius.data.api.OffenderManager[].class);

        assertThat(offenderManagers).hasSize(1);
    }

    @Test
    public void canGetOffenderManagersByOffenderCrn() {
        Mockito.when(offenderRepository.findByCrn(eq("A123"))).thenReturn(Optional.of(anOffender()));


        uk.gov.justice.digital.delius.data.api.OffenderManager[] offenderManagers = given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/crn/A123/offenderManagers")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(uk.gov.justice.digital.delius.data.api.OffenderManager[].class);

        assertThat(offenderManagers).hasSize(1);
    }

}
