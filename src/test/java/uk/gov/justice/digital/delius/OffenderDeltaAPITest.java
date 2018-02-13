package uk.gov.justice.digital.delius;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

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

        List<OffenderDelta> deltas = someDeltas(now, 20l);
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
                delta -> jdbcTemplate.update("INSERT INTO OFFENDER_DELTA(OFFENDER_ID, DATE_CHANGED, ACTION) VALUES (?, ?, ?)", delta.getOffenderId(), delta.getDateChanged(), delta.getAction())
        );
    }

    private List<OffenderDelta> someDeltas(LocalDateTime now, Long howMany) {

        return LongStream.rangeClosed(1, howMany).mapToObj(l -> OffenderDelta.builder()
                .offenderId(l)
                .dateChanged(now.minusDays(howMany / 2).plusDays(l))
                .action("UPSERT")
                .build()).collect(Collectors.toList());
    }


    @Test
    public void canDeleteOffenderDeltasOlderThan() {
        LocalDateTime now = LocalDateTime.now();

        List<OffenderDelta> deltas = someDeltas(now, 20l);
        insert(deltas);

        given()
                .when()
                .log().all()
                .queryParam("before", now.toString())
                .delete("/offenderDeltaIds")
                .then()
                .statusCode(200);

        Function<LocalDateTime, Predicate<OffenderDelta>> laterOrEqualTo = dateTime -> delta -> delta.getDateChanged().compareTo(dateTime) >= 0;
        assertThat(offenderDeltaService.findAll()).isEqualTo(
                deltas.stream().filter(laterOrEqualTo.apply(now)).collect(Collectors.toList()));
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
