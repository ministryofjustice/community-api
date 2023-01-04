package uk.gov.justice.digital.delius.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.digital.delius.controller.wiremock.DeliusExtension;
import uk.gov.justice.digital.delius.controller.wiremock.DeliusMockServer;
import uk.gov.justice.digital.delius.data.api.AccessLimitation;
import uk.gov.justice.digital.delius.data.api.Address;
import uk.gov.justice.digital.delius.data.api.Count;
import uk.gov.justice.digital.delius.data.api.DocumentMeta;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.OffenderDetailSummary;
import uk.gov.justice.digital.delius.data.api.OffenderManager;
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
import java.nio.charset.StandardCharsets;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static uk.gov.justice.digital.delius.OffenderHelper.anOffender;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"offender.ids.pagesize=5"})
@ExtendWith(SpringExtension.class)
@ActiveProfiles("dev-seed")
public class DeliusOffenderAPITest {

    private static DeliusMockServer deliusMockServer = new DeliusMockServer(8088, "src/testIntegration/resources");

    @RegisterExtension
    static DeliusExtension deliusExtension = new DeliusExtension(deliusMockServer);

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

    @BeforeEach
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
    @SuppressWarnings("unchecked")
    public void canRetrieveOffenderIdsWithDefaultPageSizeAndPage() {

        Map<?, ?> ids = given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/offenderIds")
                .then()
                .statusCode(200)
                .extract().body().as(Map.class);

        final var offenderIds = (List<Integer>) ids.get("offenderIds");

        assertThat(offenderIds).containsExactly(
                Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void canRetrieveOffenderIdsWithExplicitPageSizeAndPage() {

        Map<?, ?> ids =
                given()
                        .header("Authorization", aValidToken())
                        .when()
                        .queryParams("pageSize", 5, "page", 2)
                        .get("/offenders/offenderIds")
                        .then()
                        .statusCode(200)
                        .extract().body().as(Map.class);

        final var offenderIds = (List<Integer>) ids.get("offenderIds");

        assertThat(offenderIds).containsExactly(
                Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(10));
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
}
