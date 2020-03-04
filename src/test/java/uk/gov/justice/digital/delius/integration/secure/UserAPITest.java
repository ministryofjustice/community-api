package uk.gov.justice.digital.delius.integration.secure;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.data.api.UserDetails;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev-seed")
public class UserAPITest {

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

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
    public void retrieveStaffDetailsForMultipleUsers() throws JsonProcessingException {

        Map<?,?> staffDetails = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .body(objectMapper.writeValueAsString(Set.of("sheilahancocknps", "JimSnowLdap")))
                .when()
                .post("users/list/detail")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Map.class);

        System.out.println(staffDetails);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        UserDetails jimSnowUserDetails = (UserDetails) staffDetails.get("JimSnowLdap");
        //var sheilaHancockUserDetails = Arrays.stream(staffDetails).filter(s -> s.getUsername().equals("SheilaHancockNPS")).findFirst().get();

        assertThat(staffDetails.size()).isEqualTo(2);

        assertThat(jimSnowUserDetails.getEmail()).isEqualTo("jim.snow@justice.gov.uk");
//
//        assertThat(sheilaHancockUserDetails.getEmail()).isEqualTo("sheila.hancock@justice.gov.uk");
//        assertThat(sheilaHancockUserDetails.getStaff().getForenames()).isEqualTo("SHEILA LINDA");
//        assertThat(sheilaHancockUserDetails.getStaff().getSurname()).isEqualTo("HANCOCK");
    }

//    @Test
//    public void retrieveDetailsWhenUsersDoNotExist() {
//
//        val staffDetails = given()
//                .auth()
//                .oauth2(validOauthToken)
//                .contentType(APPLICATION_JSON_VALUE)
//                .body(getUsernames(Set.of("xxxppp1ps", "dddiiiyyyLdap")))
//                .when()
//                .post("staff/list")
//                .then()
//                .statusCode(200)
//                .extract()
//                .body()
//                .as(StaffDetails[].class);
//
//        assertThat(staffDetails).isEmpty();
//    }
//
//    @Test
//    public void retrieveMultipleUserDetailsWithNoBodyContentReturn400() {
//
//            given()
//                .auth()
//                .oauth2(validOauthToken)
//                .contentType(APPLICATION_JSON_VALUE)
//                .when()
//                .post("staff/list")
//                .then()
//                .statusCode(400);
//    }

}
