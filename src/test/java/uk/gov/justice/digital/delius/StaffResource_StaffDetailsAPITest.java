package uk.gov.justice.digital.delius;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import lombok.val;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.service.StaffService;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class StaffResource_StaffDetailsAPITest {

        @LocalServerPort
        int port;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private StaffService staffService;

        @Value("${test.token.good}")
        private String validOauthToken;

        @Before
        public void setup() {
                RestAssured.port = port;
                RestAssured.basePath = "/secure";
                RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                                new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
        }

        @Test
        public void canRetrieveStaffDetailsByStaffCode() throws JsonProcessingException {
                when(staffService.getStaffDetails("ABCDEF1")).thenReturn(Optional.of(StaffDetails.builder().build()));

                val staffDetails =  given()
                        .auth()
                        .oauth2(validOauthToken)
                        .contentType(APPLICATION_JSON_VALUE)
                        .when()
                        .get("staff/staffCode/ABCDEF1")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(StaffDetails.class);

                assertThat(staffDetails).isNotNull();
        }


        @Test
        public void retrievingStaffDetailsReturn404WhenStaffDoesNotExist() throws JsonProcessingException {
                when(staffService.getStaffDetails("ABCDEF1")).thenReturn(Optional.empty());

                given()
                        .auth()
                        .oauth2(validOauthToken)
                        .contentType(APPLICATION_JSON_VALUE)
                        .when()
                        .get("staff/staffCode/ABCDEF1")
                        .then()
                        .statusCode(404);
        }
}
