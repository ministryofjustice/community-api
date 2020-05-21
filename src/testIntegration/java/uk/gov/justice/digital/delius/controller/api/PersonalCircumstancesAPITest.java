package uk.gov.justice.digital.delius.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.PersonalCircumstance;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.user.UserData;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
public class PersonalCircumstancesAPITest {

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Jwt jwt;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));
    }

    @Test
    public void canGetPersonalCircumstancesByCrn() {

        PersonalCircumstance[] personalCircumstances = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/crn/X320741/personalCircumstances")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(PersonalCircumstance[].class);

        assertThat(personalCircumstances).extracting(PersonalCircumstance::getPersonalCircumstanceId).containsExactlyInAnyOrder(2500064995L);
    }

    @Test
    public void personalCircumstanceByCrnMustHaveValidJwt() {
        given()
            .when()
            .get("offenders/crn/CRN1/personalCircumstances")
            .then()
            .statusCode(401);
    }

    private PersonalCircumstance aPersonalCircumstance(Long id, String typeDescription, String subTypeDescription) {
        return PersonalCircumstance.builder()
            .personalCircumstanceId(id)
            .personalCircumstanceType(KeyValue.builder().code("X").description(typeDescription).build())
            .personalCircumstanceSubType(KeyValue.builder().code("X").description(subTypeDescription).build())
            .build();
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
