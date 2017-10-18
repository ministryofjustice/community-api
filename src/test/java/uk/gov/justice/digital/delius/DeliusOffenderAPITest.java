package uk.gov.justice.digital.delius;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.jpa.entity.Offender;
import uk.gov.justice.digital.delius.jpa.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.user.UserData;

import java.time.LocalDate;
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

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/delius";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));
        Mockito.when(offenderRepository.findByOffenderId(any(Long.class))).thenReturn(Optional.empty());


    }

    @Test
    public void lookupUnknownOffenderGivesNotFound() {
        given()
                .header("Authorization", aToken())
                .when()
                .get("/offenders/987654321")
                .then()
                .statusCode(404);
    }

    @Test
    public void lookupKnownOffenderGivesDetail() {

        Offender offender = Offender.builder()
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
                .interpreterRequired('N')
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
                .build();

        Mockito.when(offenderRepository.findByOffenderId(eq(1L))).thenReturn(Optional.of(
                offender
        ));

        OffenderDetail offenderDetail =
                given()
                        .header("Authorization", aToken())
                        .when()
                        .get("/offenders/1")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(OffenderDetail.class);

        assertThat(offenderDetail.getSurname().equals("Sykes"));
    }

    @Test
    public void cannotGetOffenderWithoutJwtAuthorizationHeader() {
        when()
                .get("/offenders/1")
                .then()
                .statusCode(401);
    }

    @Test
    public void cannotGetOffenderWithSomeoneElsesJwtAuthorizationHeader() {
        given()
                .header("Authorization", someoneElsesToken())
                .when()
                .get("/offenders/1")
                .then()
                .statusCode(401);
    }

    @Test
    public void cannotGetOffenderWithJunkJwtAuthorizationHeader() {
        given()
                .header("Authorization", UUID.randomUUID().toString())
                .when()
                .get("/offenders/1")
                .then()
                .statusCode(401);
    }

    private String someoneElsesToken() {
        return "Bearer " + new Jwt("Someone elses secret", 1).buildToken(UserData.builder().distinguishedName(UUID.randomUUID().toString()).build());
    }

    private String aToken() {
        return "Bearer " + jwt.buildToken(UserData.builder().distinguishedName(UUID.randomUUID().toString()).build());
    }

}