package uk.gov.justice.digital.delius.integration.secure;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.digital.delius.jwt.JwtAuthenticationHelper;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
@ExtendWith(SpringExtension.class)
public class RequirementsAPITest {


    @LocalServerPort
    int port;

    @Autowired
    protected JwtAuthenticationHelper jwtAuthenticationHelper;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    public void getRequirementsByConvictionId_convictionsFound_returnsOk() {

        var jwt = jwtAuthenticationHelper.createJwt(JwtAuthenticationHelper.JwtParameters.builder()
                    .username("APIUser")
                    .roles(List.of("ROLE_COMMUNITY"))
                    .scope(Arrays.asList("read", "write"))
                    .expiryTime(Duration.ofDays(1))
                    .build());

        given()
                .auth()
                .oauth2(jwt)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format("/offenders/crn/%s/convictions/%s/requirements", "X320741", "2500295343"))
                .then()
                .statusCode(200)
                .body("requirements", hasSize(2))
                .body("requirements[0].requirementId", equalTo(2500083652L))
                .body("requirements[0].requirementTypeMainCategory.description", equalTo("Unpaid Work"))
                .body("requirements[0].requirementTypeSubCategory.description", equalTo("Regular"))
                .body("requirements[0].startDate", equalTo("2017-06-01"))
                .body("requirements[0].terminationDate", equalTo("2017-12-01"))
                .body("requirements[0].length", equalTo(60))
                .body("requirements[0].lengthUnit", equalTo("Hours"))
                .body("requirements[0].terminationReason.description", equalTo("Hours Completed Outside 12 months (UPW only)"))

                .body("requirements[1].requirementId", equalTo(2500007925L))
                .body("requirements[1].adRequirementTypeMainCategory.description", equalTo("Court - Accredited Programme"))
                .body("requirements[1].adRequirementTypeSubCategory.description", equalTo("ASRO"))
        ;
    }
}
