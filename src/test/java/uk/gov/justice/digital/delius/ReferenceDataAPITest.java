package uk.gov.justice.digital.delius;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.ProbationArea;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.service.ReferenceDataService;
import uk.gov.justice.digital.delius.user.UserData;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class ReferenceDataAPITest {

    @LocalServerPort
    int port;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ReferenceDataService referenceDataService;
    @Autowired
    private Jwt jwt;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));

        when(referenceDataService.getProbationAreas(eq(Optional.empty()), anyBoolean())).thenReturn(someProbationAreas());
        when(referenceDataService.getProbationAreasForCode(eq("P01"), anyBoolean())).thenReturn(ImmutableList.of(aProbationArea(1)));
    }

    private ProbationArea aProbationArea(int i) {
        return ProbationArea.builder()
                .code("P0" + i)
                .description("Probation" + i)
                .build();
    }

    private List<ProbationArea> someProbationAreas() {
        return ImmutableList.of(
                aProbationArea(1),
                aProbationArea(2));
    }

    @Test
    public void canGetAllProbationAreas() {
        ProbationArea[] probationAreas = given()
                .when()
                .header("Authorization", aValidToken())
                .get("/probationAreas")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(ProbationArea[].class);

        assertThat(probationAreas).extracting("code").containsOnly("P01", "P02");
    }

    @Test
    public void canGetProbationAreasForCode() {
        ProbationArea[] probationAreas = given()
                .when()
                .header("Authorization", aValidToken())
                .get("/probationAreas/code/P01")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(ProbationArea[].class);

        assertThat(probationAreas).extracting("code").containsOnly("P01");
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
