package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@ExtendWith(SpringExtension.class)
public class RequirementsAPITest extends IntegrationTestBase {
    @DisplayName("Known CRN and conviction ID returns full set of data")
    @Test
    public void getRequirementsByConvictionId_convictionsFound_returnsOk() {

        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format("/offenders/crn/%s/convictions/%s/requirements", "X320741", "2500295343"))
                .then()
                .statusCode(HttpStatus.OK.value())
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

    @DisplayName("Unknown CRN gets a 404")
    @Test
    public void getRequirementsByUnknownCrn_returns404() {

        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(String.format("/offenders/crn/%s/convictions/%s/requirements", "XX?XX!", "2500295343"))
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
        ;
    }

    @DisplayName("Known CRN with non-matching conviction ID gives 200 with empty requirements list")
    @Test
    public void getRequirementsByConvictionId_crnFound_noConvictionsFound_returnsOk() {

        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(String.format("/offenders/crn/%s/convictions/%s/requirements", "X320741", "0"))
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("requirements", hasSize(0))
        ;
    }
}
