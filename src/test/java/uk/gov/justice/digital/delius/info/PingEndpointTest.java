package uk.gov.justice.digital.delius.info;

import io.restassured.RestAssured;
import lombok.experimental.var;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class PingEndpointTest {

    @LocalServerPort
    int port;

    @Before
    public void setup() {
        RestAssured.port = port;
    }

    @Test
    public void healthEndpointIsUnauthorised() {
        var responseBody = given()
                .when()
                .get("api/ping")
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .asString();

        assertThat(responseBody).isEqualTo("pong");

    }

}
