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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.UserAndLdap;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.service.wrapper.UserRepositoryWrapper;
import uk.gov.justice.digital.delius.user.UserData;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("user")
//@DirtiesContext
public class UserAPITest {

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Jwt jwt;

    @MockBean
    private UserRepositoryWrapper userRepositoryWrapper;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));

        when(userRepositoryWrapper.findBySurnameIgnoreCase(anyString())).thenReturn(someUsers());
        when(userRepositoryWrapper.findBySurnameIgnoreCaseAndForenameIgnoreCase(anyString(), anyString())).thenReturn(someUsers());

    }

    private List<User> someUsers() {
        return ImmutableList.of(
                User.builder().userId(1l).distinguishedName("oliver.connolly").build()
        );
    }

    @After
    public void tearDown() {
    }

    @Test
    public void canSearchForUserSurname() {
        UserAndLdap[] users = given()
                .header("Authorization", aValidTokenFor("uid=oliver.connolly,ou=people,dc=memorynotfound,dc=com"))
                .when()
                .queryParam("surname", "connolly")
                .get("/users")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(UserAndLdap[].class);

        assertThat(users).extracting("user").extracting("userId").containsOnly(1L);

        assertThat(users[0].getLdapMatches().get(0).get("entryDN").equals("uid=oliver.connolly,ou=people,dc=memorynotfound,dc=com"));


    }

    @Test
    public void canSearchForUserSurnameAndForename() {
        UserAndLdap[] users = given()
                .header("Authorization", aValidTokenFor("uid=oliver.connolly,ou=people,dc=memorynotfound,dc=com"))
                .when()
                .queryParam("surname", "connolly")
                .queryParam("forename", "oliver")
                .get("/users")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(UserAndLdap[].class);

        assertThat(users).extracting("user").extracting("userId").containsOnly(1L);

        assertThat(users[0].getLdapMatches().get(0).get("entryDN").equals("uid=oliver.connolly,ou=people,dc=memorynotfound,dc=com"));

    }

    @Test
    public void canLookupLdapEntry() {
        Map[] ldapEntry = given()
                .header("Authorization", aValidTokenFor("uid=oliver.connolly,ou=people,dc=memorynotfound,dc=com"))
                .when()
                .queryParam("field", "uid")
                .queryParam("value", "oliver.connolly")
                .get("/ldap")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Map[].class);

        assertThat(ldapEntry).isNotNull();
        assertThat(ldapEntry[0].get("entryDN")).isEqualTo("uid=oliver.connolly,ou=people,dc=memorynotfound,dc=com");

    }

    private String aValidTokenFor(String distinguishedName) {
        return "Bearer " + jwt.buildToken(UserData.builder()
                .distinguishedName(distinguishedName)
                .uid("bobby.davro").build());
    }


}
