package uk.gov.justice.digital.delius.controller.secure;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.gov.justice.digital.delius.data.api.AppointmentOutcome;
import uk.gov.justice.digital.delius.data.api.ContactSummary;
import uk.gov.justice.digital.delius.data.api.ContactType;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.OfficeLocation;
import uk.gov.justice.digital.delius.data.api.StaffHuman;
import uk.gov.justice.digital.delius.helpers.CurrentUserSupplier;
import uk.gov.justice.digital.delius.jpa.filters.ContactFilter;
import uk.gov.justice.digital.delius.service.AssessmentService;
import uk.gov.justice.digital.delius.service.ContactService;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.CustodyService;
import uk.gov.justice.digital.delius.service.NsiService;
import uk.gov.justice.digital.delius.service.OffenderManagerService;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.SentenceService;
import uk.gov.justice.digital.delius.service.TierService;
import uk.gov.justice.digital.delius.service.UserAccessService;
import uk.gov.justice.digital.delius.service.UserService;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OffendersResource_getOffenderContactSummariesByCrn {
    @Mock private OffenderService offenderService;
    @Mock private ContactService contactService;
    @Mock private ConvictionService convictionService;
    @Mock private NsiService nsiService;
    @Mock private OffenderManagerService offenderManagerService;
    @Mock private SentenceService sentenceService;
    @Mock private UserService userService;
    @Mock private CurrentUserSupplier currentUserSupplier;
    @Mock private CustodyService custodyService;
    @Mock private UserAccessService userAccessService;
    @Mock private AssessmentService assessmentService;
    @Mock private TierService tierService;
    @InjectMocks private OffendersResource subject;
    @Captor private ArgumentCaptor<ContactFilter> filterCaptor;

    @BeforeEach
    public void setUp() {
        RestAssuredMockMvc.standaloneSetup(subject);
    }

    @Test
    public void gettingContactSummariesByCrn() {
        final var contacts = List.of(aContactSummary(1L), aContactSummary(2L));
        when(offenderService.offenderIdOfCrn("CRN1")).thenReturn(Optional.of(123L));
        when(contactService.contactSummariesFor(eq(123L), filterCaptor.capture(), eq(10), eq(20)))
            .thenReturn(new PageImpl(contacts, PageRequest.of(10, 20), 1000));

        given()
            .when()
            .get("/secure/offenders/crn/CRN1/contact-summary?page=10&pageSize=20&from=2021-05-26T00:00:00Z&to=2021-06-02T00:00:00Z&contactTypes=CT1,CT2&appointmentsOnly=true&convictionId=56331&attended=true&complied=true&nationalStandard=true&outcome=true&rarActivity=true")
            .then()
            .statusCode(200)
            .body("number", equalTo(10))
            .body("first", equalTo(false))
            .body("last", equalTo(false))
            .body("totalPages", equalTo(50))
            .body("totalElements", equalTo(1000))
            .body("size", equalTo(20))
            .body("content.size()", equalTo(2))
            .body("content.find { it.contactId == 1 }", notNullValue())
            .body("content.find { it.contactId == 2 }", notNullValue());

        assertThat(filterCaptor.getValue())
            .isEqualTo(ContactFilter.builder()
                .from(Optional.of(LocalDateTime.of(2021, 5, 26, 0, 0)))
                .to(Optional.of(LocalDateTime.of(2021, 6, 2, 0, 0)))
                .contactTypes(Optional.of(List.of("CT1", "CT2")))
                .appointmentsOnly(Optional.of(true))
                .convictionId(Optional.of(56331L))
                .attended(Optional.of(true))
                .complied(Optional.of(true))
                .nationalStandard(Optional.of(true))
                .outcome(Optional.of(true))
                .rarActivity(Optional.of(true))
                .build());
    }

    private static ContactSummary aContactSummary(Long id) {
        return ContactSummary.builder()
            .contactId(id)
            .contactStart(OffsetDateTime.now())
            .contactEnd(OffsetDateTime.now().plus(1, ChronoUnit.HOURS))
            .type(ContactType.builder()
                .code("ABC123")
                .description("Some contact type description")
                .shortDescription("Some contact type short description")
                .appointment(true)
                .build())
            .officeLocation(OfficeLocation.builder()
                .code("ASP_ASH")
                .description("Ashley House Approved Premises")
                .buildingName("Ashley House")
                .buildingNumber("14")
                .streetName("Somerset Street")
                .townCity("Bristol")
                .county("Somerset")
                .postcode("BS2 8NB")
                .build())
            .notes("Some important notes about this appointment.")
            .provider(new KeyValue("P123", "Some provider"))
            .team(new KeyValue("T123", "Some team"))
            .staff(StaffHuman.builder().code("S123").forenames("Alex").surname("Haslehurst").build())
            .sensitive(true)
            .outcome(AppointmentOutcome.builder()
                .code("ABC123")
                .attended(true)
                .complied(true)
                .hoursCredited(1.5)
                .build())
            .build();
    }
}
