package uk.gov.justice.digital.delius;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.LocalServerPort;
import uk.gov.justice.digital.delius.jwt.Jwt;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("dev-seed")
//@DirtiesContext
//@RunWith(SpringJUnit4ClassRunner.class)
public class OffendersResource_AllocatePrisonOffenderManagerAPITest {

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Flyway flyway;

    @Autowired
    private Jwt jwt;


    @Value("${test.token.good}")
    private String validOauthToken;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
    }

    @After
    public void after() {
        flyway.clean();
        flyway.migrate();
    }

}
