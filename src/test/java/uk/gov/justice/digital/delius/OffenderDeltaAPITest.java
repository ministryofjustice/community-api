package uk.gov.justice.digital.delius;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.jpa.dao.OffenderDelta;
import uk.gov.justice.digital.delius.service.OffenderDeltaService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"offender.ids.pagesize=5"})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class OffenderDeltaAPITest {

    @LocalServerPort
    int port;

    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    OffenderDeltaService offenderDeltaService;
    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));
    }

    @After
    public void tearDown() {
        jdbcTemplate.execute("DELETE FROM OFFENDER_DELTA");
    }

    @Test
    public void canGetOffenderDeltas() {

        LocalDateTime now = LocalDateTime.now();

        List<OffenderDelta> deltas = someDeltas(now);
        insert(deltas);

        OffenderDelta[] offenderDeltas = given()

                .when()
                .get("/offenderDeltaIds")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(OffenderDelta[].class);

        List<OffenderDelta> offenderDeltaList = Arrays.asList(offenderDeltas);

        assertThat(offenderDeltaList).isEqualTo(deltas);
    }

    public void insert(List<OffenderDelta> deltas) {
        deltas.stream().forEach(
                delta -> jdbcTemplate.update("INSERT INTO OFFENDER_DELTA(OFFENDER_ID, DATE_CHANGED) VALUES (?, ?)", delta.getOffenderId(), delta.getDateChanged())
        );
    }

    private List<OffenderDelta> someDeltas(LocalDateTime now) {
        return ImmutableList.of(
                OffenderDelta.builder()
                        .offenderId(1L)
                        .dateChanged(now.minusDays(1L))
                        .build(),

                OffenderDelta.builder()
                        .offenderId(2L)
                        .dateChanged(now)
                        .build()
        );
    }

    @Test
    public void canDeleteOffenderDeltasOlderThan() {
        LocalDateTime now = LocalDateTime.now();

        List<OffenderDelta> deltas = someDeltas(now);
        insert(deltas);

        given()
                .when()
                .log().all()
                .queryParam("before", now.toString())
                .delete("/offenderDeltaIds")
                .then()
                .statusCode(200);

        assertThat(offenderDeltaService.findAll()).containsOnly(deltas.get(1));

    }

    @Test
    public void beforeParameterIsMandatoryForDelete() {
        given()
                .when()
                .log().all()
                .delete("/offenderDeltaIds")
                .then()
                .statusCode(400);
    }

}
