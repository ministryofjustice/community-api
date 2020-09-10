package uk.gov.justice.digital.delius.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.digital.delius.OffenderDeltaHelper;
import uk.gov.justice.digital.delius.jpa.dao.OffenderDelta;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.service.OffenderDeltaService;
import uk.gov.justice.digital.delius.user.UserData;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"offender.ids.pagesize=5"})
@ActiveProfiles("dev-schema")
public class OffenderDeltaAPITest {

    @LocalServerPort
    int port;

    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    OffenderDeltaService offenderDeltaService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private Jwt jwt;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));
    }

    @AfterEach
    public void tearDown() {
        jdbcTemplate.execute("DELETE FROM OFFENDER_DELTA");
    }

    @Test
    public void canGetOffenderDeltas() {

        final var now = LocalDateTime.now();

        final var deltas = OffenderDeltaHelper.someDeltas(now, 20l);
        OffenderDeltaHelper.insert(deltas, jdbcTemplate);
        final var expectedOffenderIds = deltas.stream()
                .map(uk.gov.justice.digital.delius.jpa.standard.entity.OffenderDelta::getOffenderId)
                .collect(Collectors.toList());

        final var offenderDeltas = given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenderDeltaIds")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(OffenderDelta[].class);

        final var offenderDeltaList = Arrays.asList(offenderDeltas);

        assertThat(offenderDeltaList).extracting(OffenderDelta::getOffenderId).containsExactlyInAnyOrderElementsOf(expectedOffenderIds);
    }

    @Test
    public void limitsOffenderDeltasTo1000() {

        final var now = LocalDateTime.now();

        final var deltas = OffenderDeltaHelper.someDeltas(now, 2000l);
        OffenderDeltaHelper.insert(deltas, jdbcTemplate);

        final var offenderDeltas = given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenderDeltaIds")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(OffenderDelta[].class);

        final var offenderDeltaList = Arrays.asList(offenderDeltas);

        assertThat(offenderDeltaList).hasSize(1000);
    }


    @Test
    public void canDeleteOffenderDeltasOlderThan() {
        final var now = LocalDateTime.now();

        final var deltas = OffenderDeltaHelper.someDeltas(now, 20l);
        OffenderDeltaHelper.insert(deltas, jdbcTemplate);

        given()
                .header("Authorization", aValidToken())
                .when()
                .log().all()
                .queryParam("before", now.toString())
                .delete("/offenderDeltaIds")
                .then()
                .statusCode(200);

        final Function<LocalDateTime, Predicate<uk.gov.justice.digital.delius.jpa.standard.entity.OffenderDelta>> laterOrEqualTo = dateTime -> delta -> delta.getDateChanged().compareTo(dateTime) >= 0;
        final var expectedOffenderIds = deltas.stream()
                .filter(laterOrEqualTo.apply(now))
                .map(uk.gov.justice.digital.delius.jpa.standard.entity.OffenderDelta::getOffenderId)
                .collect(Collectors.toList());

        assertThat(offenderDeltaService.findAll()).extracting(OffenderDelta::getOffenderId).containsExactlyInAnyOrderElementsOf(expectedOffenderIds);
    }

    @Test
    public void beforeParameterIsMandatoryForDelete() {
        given()
                .header("Authorization", aValidToken())
                .when()
                .log().all()
                .delete("/offenderDeltaIds")
                .then()
                .statusCode(400);
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
