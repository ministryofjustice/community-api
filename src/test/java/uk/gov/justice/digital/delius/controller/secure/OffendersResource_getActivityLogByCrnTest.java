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
import uk.gov.justice.digital.delius.data.api.ActivityLogGroup;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.Sentence;
import uk.gov.justice.digital.delius.helpers.CurrentUserSupplier;
import uk.gov.justice.digital.delius.jpa.filters.ContactFilter;
import uk.gov.justice.digital.delius.jpa.filters.ContactFilter.ConvictionDatesFilter;
import uk.gov.justice.digital.delius.service.AssessmentService;
import uk.gov.justice.digital.delius.service.ContactService;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.CustodyService;
import uk.gov.justice.digital.delius.service.NsiService;
import uk.gov.justice.digital.delius.service.OffenderManagerService;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.SentenceService;
import uk.gov.justice.digital.delius.service.UserAccessService;
import uk.gov.justice.digital.delius.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OffendersResource_getActivityLogByCrnTest {
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
    @InjectMocks private OffendersResource subject;
    @Captor private ArgumentCaptor<ContactFilter> filterCaptor;

    @BeforeEach
    public void setUp() {
        RestAssuredMockMvc.standaloneSetup(subject);
    }

    @Test
    void gettingActivityLogByCrn() {
        final var conviction = Conviction.builder()
            .convictionId(20L)
            .convictionDate(LocalDate.of(2021, 2, 1))
            .sentence(Sentence.builder()
                .terminationDate(LocalDate.of(2021, 3, 1))
                .build())
            .build();
        final var groups = List.of(
            anActivityLogGroup(LocalDate.of(2021, 2, 1)),
            anActivityLogGroup(LocalDate.of(2021, 2, 2))
        );

        when(offenderService.offenderIdOfCrn("CRN1")).thenReturn(Optional.of(10L));
        when(convictionService.convictionFor(10L, 20L)).thenReturn(Optional.of(conviction));
        when(contactService.activityLogFor(filterCaptor.capture(), eq(1), eq(2)))
            .thenReturn(new PageImpl<>(groups, PageRequest.of(1, 2), 1000));

        final var body = given()
            .when()
            .get("/secure/offenders/crn/CRN1/activity-log?" +
                "page=1&pageSize=2&" +
                "contactTypes=CT1&" +
                "contactDateFrom=2021-05-26&" +
                "contactDateTo=2021-06-02&" +
                "appointmentsOnly=true&" +
                "convictionId=56331&" +
                "convictionDatesOf=20&" +
                "attended=true&" +
                "complied=true&" +
                "nationalStandard=true&" +
                "outcome=true&" +
                "rarActivity=true")
            .then()
            .statusCode(200)
            .body("number", equalTo(1))
            .body("size", equalTo(2))
            .body("first", equalTo(false))
            .body("last", equalTo(false))
            .body("totalPages", equalTo(500))
            .body("totalElements", equalTo(1000))
            .body("content.size()", equalTo(2));

        assertThat(filterCaptor.getValue())
            .isEqualTo(ContactFilter.builder()
                .offenderId(10L)
                .contactTypes(Optional.of(List.of("CT1")))
                .contactDateFrom(Optional.of(LocalDate.of(2021, 5, 26)))
                .contactDateTo(Optional.of(LocalDate.of(2021, 6, 2)))
                .appointmentsOnly(Optional.of(true))
                .convictionId(Optional.of(56331L))
                .convictionDatesOf(Optional.of(new ConvictionDatesFilter(20L,
                    Optional.of(LocalDate.of(2021, 2, 1)),
                    Optional.of(LocalDate.of(2021, 3, 1)))))
                .attended(Optional.of(true))
                .complied(Optional.of(true))
                .nationalStandard(Optional.of(true))
                .outcome(Optional.of(true))
                .rarActivity(Optional.of(true))
                .build());
    }

    @Test
    void gettingActivityLogEntryWithNoSentence() {
        final var conviction = Conviction.builder()
            .convictionId(20L)
            .convictionDate(LocalDate.of(2021, 2, 1))
            .sentence(null)
            .build();

        final var groups = List.of(
            anActivityLogGroup(LocalDate.of(2021, 2, 1))
        );

        when(offenderService.offenderIdOfCrn("CRN1")).thenReturn(Optional.of(10L));
        when(convictionService.convictionFor(10L, 20L)).thenReturn(Optional.of(conviction));
        when(contactService.activityLogFor(filterCaptor.capture(), eq(1), eq(2)))
            .thenReturn(new PageImpl<>(groups, PageRequest.of(1, 2), 1000));

        final var body = given()
            .when()
            .get("/secure/offenders/crn/CRN1/activity-log?" +
                "page=1&pageSize=2&" +
                "convictionDatesOf=20&")
            .then()
            .statusCode(200);

        assertThat(filterCaptor.getValue())
            .isEqualTo(ContactFilter.builder()
                .offenderId(10L)
                .convictionDatesOf(Optional.of(new ConvictionDatesFilter(20L,
                    Optional.of(LocalDate.of(2021, 2, 1)),
                    Optional.empty())))
                .build());
    }

    private static ActivityLogGroup anActivityLogGroup(LocalDate date) {
        return ActivityLogGroup.builder()
            .date(date)
            .rarDay(true)
            .entries(List.of())
            .build();
    }
}
