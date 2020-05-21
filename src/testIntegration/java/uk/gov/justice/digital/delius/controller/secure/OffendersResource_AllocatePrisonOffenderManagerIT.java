package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.Contact;
import uk.gov.justice.digital.delius.data.api.CreatePrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.Human;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.user.UserData;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
@DirtiesContext
@RunWith(SpringJUnit4ClassRunner.class)
public class OffendersResource_AllocatePrisonOffenderManagerIT {

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Flyway flyway;

    @Autowired
    private Jwt jwt;


    @Value("${test.token.good}")
    private String validOauthToken;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
    }

    @After
    public void after() {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void canAllocatePrisonOffenderManagersByNOMSNumberAndStaffCode() throws JsonProcessingException {
        final var offenderManagersBeforeAllocation = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/G0560UO/allOffenderManagers")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager[].class);

        assertThat(prisonOffenderManager(offenderManagersBeforeAllocation)).isNotPresent();
        assertThat(communityOffenderManager(offenderManagersBeforeAllocation)).isPresent();
        assertThat(communityOffenderManager(offenderManagersBeforeAllocation).orElseThrow().getIsResponsibleOfficer()).isTrue();

        final var newPrisonOffenderManager = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf("BWIA010", "BWI"))
                .when()
                .put("/offenders/nomsNumber/G0560UO/prisonOffenderManager")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager.class);

        assertThat(newPrisonOffenderManager.getStaffCode()).isEqualTo("BWIA010");

        // OM Team will be POM team in area within the POM borough, district and LDU
        assertThat(newPrisonOffenderManager.getTeam().getDescription()).isEqualTo("Prison Offender Managers");
        assertThat(newPrisonOffenderManager.getTeam().getCode()).isEqualTo("BWIPOM");
        assertThat(newPrisonOffenderManager.getTeam().getBorough().getDescription()).isEqualTo("Prison Offender Managers");
        assertThat(newPrisonOffenderManager.getTeam().getBorough().getCode()).isEqualTo("BWIPOM");
        assertThat(newPrisonOffenderManager.getTeam().getDistrict().getDescription()).isEqualTo("Prison Offender Managers");
        assertThat(newPrisonOffenderManager.getTeam().getDistrict().getCode()).isEqualTo("BWIPOM");
        assertThat(newPrisonOffenderManager.getTeam().getLocalDeliveryUnit().getDescription()).isEqualTo("Prison Offender Managers");
        assertThat(newPrisonOffenderManager.getTeam().getLocalDeliveryUnit().getCode()).isEqualTo("BWIPOM");
        // OM will be in Prison Probation Area
        assertThat(newPrisonOffenderManager.getProbationArea().getDescription()).isEqualTo("Berwyn (HMP)");
        assertThat(newPrisonOffenderManager.getProbationArea().getCode()).isEqualTo("BWI");

        final var staffDetails = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("staff/staffCode/BWIA010")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(StaffDetails.class);

        // Staff will now be in the POM Team
         assertThat(staffDetails.getTeams()).contains(newPrisonOffenderManager.getTeam());

        final var offenderManagers = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/G0560UO/allOffenderManagers")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager[].class);

        // should have both community and prison offender manager
        assertThat(prisonOffenderManager(offenderManagers)).isPresent();
        assertThat(communityOffenderManager(offenderManagers)).isPresent();
        // responsible officer remains unchanged
        assertThat(communityOffenderManager(offenderManagers).orElseThrow().getIsResponsibleOfficer()).isTrue();
        assertThat(prisonOffenderManager(offenderManagers).orElseThrow().getIsResponsibleOfficer()).isFalse();
        assertThat(prisonOffenderManager(offenderManagers).orElseThrow().getStaffCode()).isEqualTo("BWIA010");
    }

    @Test
    public void canAllocatePrisonOffenderManagersByNOMSNumberAndStaffName() throws JsonProcessingException {
        final var offenderManagersBeforeAllocation = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/G9542VP/allOffenderManagers")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager[].class);

        // assert we have a COM and POM with POM currently responsible officer
        assertThat(offenderManagersBeforeAllocation).hasSize(2);
        assertThat(prisonOffenderManager(offenderManagersBeforeAllocation)).isPresent();
        assertThat(prisonOffenderManager(offenderManagersBeforeAllocation).orElseThrow().getIsResponsibleOfficer()).isTrue();
        assertThat(prisonOffenderManager(offenderManagersBeforeAllocation).orElseThrow().getStaffCode()).isEqualTo("BWIA010");
        assertThat(communityOffenderManager(offenderManagersBeforeAllocation)).isPresent();

        final var newPrisonOffenderManager = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf(Human
                        .builder()
                        .surname("Marke")
                        .forenames("Joe")
                        .build(),
                        "BWI"))
                .when()
                .put("/offenders/nomsNumber/G9542VP/prisonOffenderManager")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager.class);

        // no longer previous POM
        assertThat(newPrisonOffenderManager.getStaffCode()).isNotEqualTo("BWIA010");
        // but will be a staff code in BWI area with area prefix
        assertThat(newPrisonOffenderManager.getStaffCode()).startsWith("BWI");

        // OM Team will be POM team in area within the POM borough, district and LDU
        assertThat(newPrisonOffenderManager.getTeam().getDescription()).isEqualTo("Prison Offender Managers");
        assertThat(newPrisonOffenderManager.getTeam().getCode()).isEqualTo("BWIPOM");
        assertThat(newPrisonOffenderManager.getTeam().getBorough().getDescription()).isEqualTo("Prison Offender Managers");
        assertThat(newPrisonOffenderManager.getTeam().getBorough().getCode()).isEqualTo("BWIPOM");
        assertThat(newPrisonOffenderManager.getTeam().getDistrict().getDescription()).isEqualTo("Prison Offender Managers");
        assertThat(newPrisonOffenderManager.getTeam().getDistrict().getCode()).isEqualTo("BWIPOM");
        assertThat(newPrisonOffenderManager.getTeam().getLocalDeliveryUnit().getDescription()).isEqualTo("Prison Offender Managers");
        assertThat(newPrisonOffenderManager.getTeam().getLocalDeliveryUnit().getCode()).isEqualTo("BWIPOM");
        // OM will be in Prison Probation Area
        assertThat(newPrisonOffenderManager.getProbationArea().getDescription()).isEqualTo("Berwyn (HMP)");
        assertThat(newPrisonOffenderManager.getProbationArea().getCode()).isEqualTo("BWI");

        final var staffDetails = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format("staff/staffCode/%s", newPrisonOffenderManager.getStaffCode()))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(StaffDetails.class);

        // New staff member will be in the POM Team
        assertThat(staffDetails.getTeams()).contains(newPrisonOffenderManager.getTeam());


        final var offenderManagers = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/G9542VP/allOffenderManagers")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager[].class);

        // should have both community and prison offender manager
        assertThat(offenderManagers).hasSize(2);
        assertThat(prisonOffenderManager(offenderManagers)).isPresent();

        // responsible officer should be the new  POM
        assertThat(prisonOffenderManager(offenderManagers).orElseThrow().getIsResponsibleOfficer()).isTrue();
        assertThat(prisonOffenderManager(offenderManagers).orElseThrow().getStaffCode()).isEqualTo(newPrisonOffenderManager.getStaffCode());
        assertThat(communityOffenderManager(offenderManagers)).isPresent();
    }

    @Test
    public void willAddAContactWhenAllocatingPrisonOffenderManager() throws JsonProcessingException {
        final var justBeforeAllocation = LocalDateTime.now().minusHours(1);

        final var newPrisonOffenderManager = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf("BWIA010", "BWI"))
                .when()
                .put("/offenders/nomsNumber/G0560UO/prisonOffenderManager")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager.class);

        assertThat(newPrisonOffenderManager.getStaffCode()).isEqualTo("BWIA010");

        Contact[] contacts = given()
                .when()
                .header("Authorization", aValidToken())
                .queryParam("from", justBeforeAllocation.toString())
                .basePath("/api")
                .get("/offenders/nomsNumber/G0560UO/contacts")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Contact[].class);

        assertThat(contacts).hasSize(1);
        assertThat(contacts[0].getContactType().getDescription()).isEqualTo("Prison Offender Manager - Automatic Transfer");
    }

    public Optional<CommunityOrPrisonOffenderManager> prisonOffenderManager(CommunityOrPrisonOffenderManager[] offenderManagers) {
        return Stream.of(offenderManagers).filter(CommunityOrPrisonOffenderManager::getIsPrisonOffenderManager).findAny();
    }

    public Optional<CommunityOrPrisonOffenderManager> communityOffenderManager(CommunityOrPrisonOffenderManager[] offenderManagers) {
        return Stream.of(offenderManagers).filter(not(CommunityOrPrisonOffenderManager::getIsPrisonOffenderManager)).findAny();
    }

    @Test
    public void shouldRespondWith404WhenAllocatingPrisonOffenderManagersAndExistingStaffNotFound() throws JsonProcessingException {
        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf("DOESNOTEXIST"))
                .when()
                .put("/offenders/nomsNumber/G9542VP/prisonOffenderManager")
                .then()
                .statusCode(404);
    }

    @Test
    public void shouldRespondWith404WhenAllocatingPrisonOffenderManagersAndOffenderNotFound() throws JsonProcessingException {
        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf("BWIA010"))
                .when()
                .put("/offenders/nomsNumber/DOESNOTEXIST/prisonOffenderManager")
                .then()
                .statusCode(404);
    }

    @Test
    public void shouldRespondWith400WhenAllocatingPrisonOffenderManagersAndPrisonInstitutionNotFound() throws JsonProcessingException {
        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf("BWIA010", "DOESNOTEXIST"))
                .when()
                .put("/offenders/nomsNumber/G9542VP/prisonOffenderManager")
                .then()
                .statusCode(400);
    }

    @Test
    public void shouldRespondWith400WhenStaffMemberNotInThePrisonInstitutionProbationArea() throws JsonProcessingException {
        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf("BWIA010", "WWI"))
                .when()
                .put("/offenders/nomsNumber/G9542VP/prisonOffenderManager")
                .then()
                .statusCode(400);
    }

    private String createPrisonOffenderManagerOf(String staffCode) throws JsonProcessingException {
        return objectMapper.writeValueAsString(CreatePrisonOffenderManager
                .builder()
                .officerCode(staffCode)
                .nomsPrisonInstitutionCode("BWI")
                .build());
    }

    private String createPrisonOffenderManagerOf(String staffCode, String nomsPrisonInstitutionCode) throws JsonProcessingException {
        return objectMapper.writeValueAsString(CreatePrisonOffenderManager
                .builder()
                .officerCode(staffCode)
                .nomsPrisonInstitutionCode(nomsPrisonInstitutionCode)
                .build());
    }

    private String createPrisonOffenderManagerOf(Human staff, String nomsPrisonInstitutionCode) throws JsonProcessingException {
        return objectMapper.writeValueAsString(CreatePrisonOffenderManager
                .builder()
                .officer(staff)
                .nomsPrisonInstitutionCode(nomsPrisonInstitutionCode)
                .build());
    }


    private String aValidToken() {
        return aValidTokenFor(UUID.randomUUID().toString());
    }

    private String aValidTokenFor(String distinguishedName) {
        return "Bearer " + jwt.buildToken(UserData.builder()
                .distinguishedName(distinguishedName)
                .uid("bobby.davro").build());
    }


}
