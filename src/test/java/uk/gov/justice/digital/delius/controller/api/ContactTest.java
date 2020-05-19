package uk.gov.justice.digital.delius.controller.api;

import com.google.common.collect.ImmutableList;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.Contact;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.jpa.filters.ContactFilter;
import uk.gov.justice.digital.delius.service.ContactService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.time.LocalDateTime;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ContactTest {

    private final LocalDateTime now = LocalDateTime.now();

    @Mock
    private ContactService contactService;

    @Mock
    private OffenderService offenderService;

    private final OffenderDetail anOffender = OffenderDetail.builder().offenderId(1L).build();
    private final ContactFilter noFilter = ContactFilter.builder().build();
    private final ContactFilter typeFilter = ContactFilter.builder().contactTypes(Optional.of(ImmutableList.of("AAAA"))).build();
    private final ContactFilter fromFilter = ContactFilter.builder().from(Optional.of(now)).build();
    private final ContactFilter toFilter = ContactFilter.builder().to(Optional.of(now)).build();
    private final Contact contact1 = aValidContactFor(1L);
    private final Contact contact2 = aValidContactFor(2L);
    private final Contact contact3 = aValidContactFor(3L);
    private final Contact contact4 = aValidContactFor(4L);


    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.standaloneSetup(
                new ContactController(offenderService, contactService)
        );
    }

    private Contact aValidContactFor(long contactId) {
        return Contact.builder()
                .contactId(contactId)
                .build();

    }

    @Test
    public void canGetAllContactsByOffenderId() {
        when(offenderService.getOffenderByOffenderId(1L)).thenReturn(Optional.of(anOffender));
        when(contactService.contactsFor(1L, noFilter)).thenReturn(ImmutableList.of(contact1, contact2, contact3, contact4));

        Contact[] contacts = given()
                .when()
                .get("/api/offenders/offenderId/1/contacts")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(1L, 2L, 3L, 4L);
    }

    @Test
    public void canFilterContactsByOffenderIdAndContactType() {
        when(offenderService.getOffenderByOffenderId(1L)).thenReturn(Optional.of(anOffender));
        when(contactService.contactsFor(1L, typeFilter)).thenReturn(ImmutableList.of(contact3, contact4));

        Contact[] contacts = given()
                .when()
                .get("/api/offenders/offenderId/1/contacts?contactTypes=AAAA")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(3L, 4L);
    }

    @Test
    public void canFilterContactsByOffenderIdAndFromDateTime() {
        when(offenderService.getOffenderByOffenderId(1L)).thenReturn(Optional.of(anOffender));
        when(contactService.contactsFor(1L, fromFilter)).thenReturn(ImmutableList.of(contact2, contact3, contact4));

        Contact[] contacts = given()
                .when()
                .get(format("/api/offenders/offenderId/1/contacts?from=%s", now.toString()))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(2L, 3L, 4L);
    }

    @Test
    public void canFilterContactsByOffenderIdAndToDateTime() {
        when(offenderService.getOffenderByOffenderId(1L)).thenReturn(Optional.of(anOffender));
        when(contactService.contactsFor(1L, toFilter)).thenReturn(ImmutableList.of(contact4));

        Contact[] contacts = given()
                .when()
                .get(format("/api/offenders/offenderId/1/contacts?to=%s", now.toString()))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(4L);
    }

    @Test
    public void canGetAllContactsByCrn() {
        when(offenderService.offenderIdOfCrn("crn1")).thenReturn(Optional.of(1L));
        when(contactService.contactsFor(1L, noFilter)).thenReturn(ImmutableList.of(contact1, contact2, contact3, contact4));

        Contact[] contacts = given()
                .when()
                .get("/api/offenders/crn/crn1/contacts")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(1L, 2L, 3L, 4L);
    }

    @Test
    public void canFilterContactsByCrnAndContactType() {
        when(offenderService.offenderIdOfCrn("crn1")).thenReturn(Optional.of(1L));
        when(contactService.contactsFor(1L, typeFilter)).thenReturn(ImmutableList.of(contact3, contact4));

        Contact[] contacts = given()
                .when()
                .get("/api/offenders/crn/crn1/contacts?contactTypes=AAAA")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(3L, 4L);
    }

    @Test
    public void canFilterContactsByCrnAndFromDateTime() {
        when(offenderService.offenderIdOfCrn("crn1")).thenReturn(Optional.of(1L));
        when(contactService.contactsFor(1L, fromFilter)).thenReturn(ImmutableList.of(contact2, contact3, contact4));

        Contact[] contacts = given()
                .when()
                .get(format("/api/offenders/crn/crn1/contacts?from=%s", now.toString()))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(2L, 3L, 4L);
    }

    @Test
    public void canFilterContactsByCrnAndToDateTime() {
        when(offenderService.offenderIdOfCrn("crn1")).thenReturn(Optional.of(1L));
        when(contactService.contactsFor(1L, toFilter)).thenReturn(ImmutableList.of(contact4));

        Contact[] contacts = given()
                .when()
                .get(format("/api/offenders/crn/crn1/contacts?to=%s", now.toString()))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(4L);
    }

    @Test
    public void canGetAllContactsByNomsNumber() {
        when(offenderService.offenderIdOfNomsNumber("noms1")).thenReturn(Optional.of(1L));
        when(contactService.contactsFor(1L, noFilter)).thenReturn(ImmutableList.of(contact1, contact2, contact3, contact4));

        Contact[] contacts = given()
                .when()
                .get("/api/offenders/nomsNumber/noms1/contacts")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(1L, 2L, 3L, 4L);
    }

    @Test
    public void canFilterContactsByNomsNumberAndContactType() {
        when(offenderService.offenderIdOfNomsNumber("noms1")).thenReturn(Optional.of(1L));
        when(contactService.contactsFor(1L, typeFilter)).thenReturn(ImmutableList.of(contact3, contact4));

        Contact[] contacts = given()
                .when()
                .get("/api/offenders/nomsNumber/noms1/contacts?contactTypes=AAAA")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(3L, 4L);
    }

    @Test
    public void canFilterContactsByNomsNumberAndFromDateTime() {
        when(offenderService.offenderIdOfNomsNumber("noms1")).thenReturn(Optional.of(1L));
        when(contactService.contactsFor(1L, fromFilter)).thenReturn(ImmutableList.of(contact2, contact3, contact4));

        Contact[] contacts = given()
                .when()
                .get(format("/api/offenders/nomsNumber/noms1/contacts?from=%s", now.toString()))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(2L, 3L, 4L);
    }

    @Test
    public void canFilterContactsByNomsNumberAndToDateTime() {
        when(offenderService.offenderIdOfNomsNumber("noms1")).thenReturn(Optional.of(1L));
        when(contactService.contactsFor(1L, toFilter)).thenReturn(ImmutableList.of(contact4));

        Contact[] contacts = given()
                .when()
                .get(format("/api/offenders/nomsNumber/noms1/contacts?to=%s", now.toString()))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Contact[].class);

        assertThat(contacts).extracting("contactId").containsOnly(4L);
    }

}
