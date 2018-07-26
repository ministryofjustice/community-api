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
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.Contact;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.jpa.filters.ContactFilter;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.service.ContactService;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.user.UserData;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class ContactAPITest {

    private final LocalDateTime now = LocalDateTime.now();
    @LocalServerPort
    int port;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ContactService contactService;
    @MockBean
    private OffenderService offenderService;
    @Autowired
    private Jwt jwt;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));

        OffenderDetail anOffender = OffenderDetail.builder().offenderId(1L).build();
        when(offenderService.getOffenderByOffenderId(1L)).thenReturn(Optional.of(anOffender));
        when(offenderService.offenderIdOfNomsNumber("noms1")).thenReturn(Optional.of(1L));
        when(offenderService.offenderIdOfCrn("crn1")).thenReturn(Optional.of(1L));


        ContactFilter noFilter = ContactFilter.builder().build();
        ContactFilter typeFilter = ContactFilter.builder().contactTypes(Optional.of(ImmutableList.of("AAAA"))).build();
        ContactFilter fromFilter = ContactFilter.builder().from(Optional.of(now)).build();
        ContactFilter toFilter = ContactFilter.builder().to(Optional.of(now)).build();

        Contact contact1 = aValidContactFor(1L);
        Contact contact2 = aValidContactFor(2L);
        Contact contact3 = aValidContactFor(3L);
        Contact contact4 = aValidContactFor(4L);

        when(contactService.contactsFor(1L, noFilter)).thenReturn(ImmutableList.of(contact1, contact2, contact3, contact4));
        when(contactService.contactsFor(1L, typeFilter)).thenReturn(ImmutableList.of(contact3, contact4));
        when(contactService.contactsFor(1L, fromFilter)).thenReturn(ImmutableList.of(contact2, contact3, contact4));
        when(contactService.contactsFor(1L, toFilter)).thenReturn(ImmutableList.of(contact4));

    }

    private Contact aValidContactFor(long contactId) {
        return Contact.builder()
                .contactId(contactId)
                .build();

    }

    @Test
    public void canGetAllContactsByOffenderId() {
        uk.gov.justice.digital.delius.data.api.Contact[] contacts = given()
                .when()
                .header("Authorization", aValidToken())
                .get("/offenders/offenderId/1/contacts")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(uk.gov.justice.digital.delius.data.api.Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(1L, 2L, 3L, 4L);
    }

    @Test
    public void canFilterContactsByOffenderIdAndContactType() {
        uk.gov.justice.digital.delius.data.api.Contact[] contacts = given()
                .when()
                .header("Authorization", aValidToken())
                .queryParam("contactTypes", "AAAA")
                .get("/offenders/offenderId/1/contacts")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(uk.gov.justice.digital.delius.data.api.Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(3L, 4L);
    }

    @Test
    public void canFilterContactsByOffenderIdAndFromDateTime() {
        uk.gov.justice.digital.delius.data.api.Contact[] contacts = given()
                .when()
                .header("Authorization", aValidToken())
                .queryParam("from", now.toString())
                .get("/offenders/offenderId/1/contacts")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(uk.gov.justice.digital.delius.data.api.Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(2L, 3L, 4L);
    }

    @Test
    public void canFilterContactsByOffenderIdAndToDateTime() {
        uk.gov.justice.digital.delius.data.api.Contact[] contacts = given()
                .when()
                .header("Authorization", aValidToken())
                .queryParam("to", now.toString())
                .get("/offenders/offenderId/1/contacts")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(uk.gov.justice.digital.delius.data.api.Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(4L);
    }

    @Test
    public void canGetAllContactsByCrn() {
        uk.gov.justice.digital.delius.data.api.Contact[] contacts = given()
                .when()
                .header("Authorization", aValidToken())
                .get("/offenders/crn/crn1/contacts")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(uk.gov.justice.digital.delius.data.api.Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(1L, 2L, 3L, 4L);
    }

    @Test
    public void canFilterContactsByCrnAndContactType() {
        uk.gov.justice.digital.delius.data.api.Contact[] contacts = given()
                .when()
                .header("Authorization", aValidToken())
                .queryParam("contactTypes", "AAAA")
                .get("/offenders/crn/crn1/contacts")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(uk.gov.justice.digital.delius.data.api.Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(3L, 4L);
    }

    @Test
    public void canFilterContactsByCrnAndFromDateTime() {
        uk.gov.justice.digital.delius.data.api.Contact[] contacts = given()
                .when()
                .header("Authorization", aValidToken())
                .queryParam("from", now.toString())
                .get("/offenders/crn/crn1/contacts")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(uk.gov.justice.digital.delius.data.api.Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(2L, 3L, 4L);
    }

    @Test
    public void canFilterContactsByCrnAndToDateTime() {
        uk.gov.justice.digital.delius.data.api.Contact[] contacts = given()
                .when()
                .header("Authorization", aValidToken())
                .queryParam("to", now.toString())
                .get("/offenders/crn/crn1/contacts")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(uk.gov.justice.digital.delius.data.api.Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(4L);
    }

    @Test
    public void canGetAllContactsByNomsNumber() {
        uk.gov.justice.digital.delius.data.api.Contact[] contacts = given()
                .when()
                .header("Authorization", aValidToken())
                .get("/offenders/nomsNumber/noms1/contacts")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(uk.gov.justice.digital.delius.data.api.Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(1L, 2L, 3L, 4L);
    }

    @Test
    public void canFilterContactsByNomsNumberAndContactType() {
        uk.gov.justice.digital.delius.data.api.Contact[] contacts = given()
                .when()
                .header("Authorization", aValidToken())
                .queryParam("contactTypes", "AAAA")
                .get("/offenders/nomsNumber/noms1/contacts")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(uk.gov.justice.digital.delius.data.api.Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(3L, 4L);
    }

    @Test
    public void canFilterContactsByNomsNumberAndFromDateTime() {
        uk.gov.justice.digital.delius.data.api.Contact[] contacts = given()
                .when()
                .header("Authorization", aValidToken())
                .queryParam("from", now.toString())
                .get("/offenders/nomsNumber/noms1/contacts")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(uk.gov.justice.digital.delius.data.api.Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(2L, 3L, 4L);
    }

    @Test
    public void canFilterContactsByNomsNumberAndToDateTime() {
        uk.gov.justice.digital.delius.data.api.Contact[] contacts = given()
                .when()
                .header("Authorization", aValidToken())
                .queryParam("to", now.toString())
                .get("/offenders/nomsNumber/noms1/contacts")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(uk.gov.justice.digital.delius.data.api.Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(4L);
    }

    @Test
    public void contactsByOffenderIdMustHaveValidJwt() {
        given()
                .when()
                .queryParam("to", now.toString())
                .get("/offenders/offenderId/1/contacts")
                .then()
                .statusCode(401);

    }

    @Test
    public void contactsByCrnMustHaveValidJwt() {
        given()
                .when()
                .queryParam("to", now.toString())
                .get("/offenders/crn/crn1/contacts")
                .then()
                .statusCode(401);

    }

    @Test
    public void contactsByNomsNumberMustHaveVaidJwt() {
        given()

                .when()
                .queryParam("to", now.toString())
                .get("/offenders/nomsNumber/noms1/contacts")
                .then()
                .statusCode(401);

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