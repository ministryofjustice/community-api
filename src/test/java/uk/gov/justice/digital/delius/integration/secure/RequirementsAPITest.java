package uk.gov.justice.digital.delius.integration.secure;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
@RunWith(SpringJUnit4ClassRunner.class)
public class RequirementsAPITest {


    @LocalServerPort
    int port;

    @Value("${test.token.good}")
    private String validOauthToken;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
    }

    @Test
    public void getRequirementsByConvictionId_convictionsFound_returnsOk() {

        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format("/offenders/crn/%s/convictions/%s/requirements", "X320741", "2500295343"))
                .then()
                .statusCode(200)
                .body("requirements", hasSize(2))
                .body("requirements[0].requirementType", equalTo("Unpaid Work"))
                .body("requirements[0].requirementTypeSubCategory", equalTo("Regular"))
                .body("requirements[0].length", equalTo("60"))
                .body("requirements[0].startDate", equalTo("2017-6-01"))
                .body("requirements[0].terminationDate", equalTo("2017-12-01"))
                .body("requirements[0].terminationReason", equalTo("Hours Completed Outside 12 months (UPW only)"))

                .body("requirements[1].adRequirementType", equalTo("Court - Accredited Programme"))
                .body("requirements[1].adRequirementTypeSubCategory", equalTo("ASRO"))
        ;
    }
}
