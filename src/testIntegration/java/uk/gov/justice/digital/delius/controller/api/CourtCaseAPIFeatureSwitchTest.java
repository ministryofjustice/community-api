package uk.gov.justice.digital.delius.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.CourtCase;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.user.UserData;

import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = {
        "features.events.experimental=false"
})
public class CourtCaseAPIFeatureSwitchTest {

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConvictionService convictionService;

    @MockBean
    private OffenderService offenderService;

    @Autowired
    private Jwt jwt;

    @Before
    public void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));

        when(offenderService.offenderIdOfCrn("CRN1")).thenReturn(Optional.of(1L));
    }

    @Test
    public void canNotAddCourCaseWhenFeatureIsSwitchOff() {

        given()
                .header("Authorization", aValidToken())
                .contentType("application/json")
                .body(aCourtCase())
                .when()
                .post("offenders/crn/CRN1/courtCase")
                .then()
                .statusCode(404);
    }

    private CourtCase aCourtCase() {
        return CourtCase.builder().build();
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
