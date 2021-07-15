package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.config.FeatureSwitches;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.CourtCase;
import uk.gov.justice.digital.delius.data.api.OffenceDetail;
import uk.gov.justice.digital.delius.entitybuilders.AdditionalOffenceEntityBuilder;
import uk.gov.justice.digital.delius.entitybuilders.CourtAppearanceEntityBuilder;
import uk.gov.justice.digital.delius.entitybuilders.EventEntityBuilder;
import uk.gov.justice.digital.delius.entitybuilders.KeyDateEntityBuilder;
import uk.gov.justice.digital.delius.entitybuilders.MainOffenceEntityBuilder;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.jpa.standard.entity.Court;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.DisposalType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offence;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.Requirement;
import uk.gov.justice.digital.delius.jpa.standard.entity.RequirementTypeMainCategory;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;
import uk.gov.justice.digital.delius.jpa.standard.entity.TransferReason;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.service.ConvictionService.DuplicateActiveCustodialConvictionsException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.jpa.standard.entity.RequirementTypeMainCategory.REHABILITATION_ACTIVITY_REQUIREMENT_CODE;

@ExtendWith(MockitoExtension.class)
class ConvictionServiceTest {

    private static final Long ANY_OFFENDER_ID = 123L;
    private static final String EXCLUSION_REQUIREMENT_CODE = "X";
    private static final String NC_SENTENCE_TYPE = "NC";

    private ConvictionService convictionService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private OffenderRepository offenderRepository;

    @Mock
    private ContactService contactService;

    @Mock
    private LookupSupplier lookupSupplier;

    @Mock
    private SpgNotificationService spgNotificationService;

    @Mock
    private KeyDateEntityBuilder keyDateEntityBuilder;

    @Mock
    private IAPSNotificationService iapsNotificationService;

    @Mock
    private TelemetryClient telemetryClient;

    @BeforeEach
    void setUp() {
        final var eventEntityBuilder = new EventEntityBuilder(
                new MainOffenceEntityBuilder(lookupSupplier),
                new AdditionalOffenceEntityBuilder(lookupSupplier),
                new CourtAppearanceEntityBuilder(lookupSupplier),
                lookupSupplier
        );
        final var featureSwitches = new FeatureSwitches();
        featureSwitches.getNoms().getUpdate().setKeyDates(true);
        convictionService = new ConvictionService(eventRepository, offenderRepository, eventEntityBuilder, spgNotificationService, lookupSupplier, keyDateEntityBuilder, iapsNotificationService, contactService, telemetryClient, featureSwitches);
    }

    @Test
    void convictionsOrderedByCreationDate() {
        when(eventRepository.findByOffenderId(1L))
                .thenReturn(ImmutableList.of(
                        aEvent().toBuilder().eventId(99L).referralDate(now().minusDays(1)).build(),
                        aEvent().toBuilder().eventId(9L).referralDate(now().minusDays(2)).build(),
                        aEvent().toBuilder().eventId(999L).referralDate(now()).build()
                ));

        assertThat(convictionService.convictionsFor(1L, false)
                .stream().map(uk.gov.justice.digital.delius.data.api.Conviction::getConvictionId)
                .collect(Collectors.toList()))
                .containsSequence(999L, 99L, 9L);

    }

    @Test
    void convictionForEventIdButIsSoftDeleted() {
        when(eventRepository.findById(99L))
            .thenReturn(Optional.of(
                aEvent().toBuilder().eventId(99L).softDeleted(true).referralDate(now().minusDays(1)).build()
            ));

        assertThat(convictionService.convictionFor(1L, 99L)).isEmpty();
    }

    @Test
    void convictionForEventId() {
        when(eventRepository.findById(99L))
            .thenReturn(Optional.of(
                aEvent().toBuilder().eventId(99L).offenderId(1L).build()
            ));

        assertThat(convictionService.convictionFor(1L, 99L)).isPresent();
    }

    @Test
    void eventForEventId() {
        var event = Event.builder().eventId(99L).build();
        when(eventRepository.findByEventIdAndOffenderIdAndSoftDeletedFalse(99L, 1L)).thenReturn(Optional.of(event));

        assertThat(convictionService.eventFor(1L, 99L)).isPresent();
    }

    @Test
    void deletedRecordsIgnored() {
        when(eventRepository.findByOffenderId(1L))
                .thenReturn(ImmutableList.of(
                        aEvent().toBuilder().eventId(1L).build(),
                        aEvent().toBuilder().eventId(2L).softDeleted(true).build(),
                        aEvent().toBuilder().eventId(3L).build()
                ));

        assertThat(convictionService.convictionsFor(1L, false)
                .stream().map(uk.gov.justice.digital.delius.data.api.Conviction::getConvictionId)
                .collect(Collectors.toList()))
                .contains(1L, 3L);


    }

    @Test
    public void addingACourtCaseCreatesAConvictionEventWithNewIndexMatchingNumberOfConvictions() {
        when(lookupSupplier.userSupplier()).thenReturn(() -> User.builder().userId(99L).build());
        when(lookupSupplier.orderAllocationReasonSupplier()).thenReturn(code -> StandardReference.builder().codeValue(code).build());
        when(lookupSupplier.transferReasonSupplier()).thenReturn(code -> TransferReason.builder().code(code).build());
        when(lookupSupplier.probationAreaSupplier()).thenReturn(orderManager -> ProbationArea.builder().probationAreaId(orderManager.getProbationAreaId()).build());
        when(lookupSupplier.teamSupplier()).thenReturn(orderManager -> Team.builder().teamId(orderManager.getTeamId()).build());
        when(lookupSupplier.staffSupplier()).thenReturn(orderManager -> Staff.builder().staffId(orderManager.getOfficerId()).build());
        when(lookupSupplier.offenceSupplier()).thenReturn(code -> Offence.builder().code(code).ogrsOffenceCategory(StandardReference.builder().build()).build());
        when(lookupSupplier.courtSupplier()).thenReturn(courtId -> Court.builder().courtId(courtId).build());

        // echo back what we saved
        when(eventRepository.save(any(Event.class))).thenAnswer((invocationOnMock -> invocationOnMock.getArgument(0)));
        when(eventRepository.findByOffenderId(1L)).thenReturn(ImmutableList.of(aEvent(), aEvent()));

        final Conviction conviction = convictionService.addCourtCaseFor(
                1L,
                CourtCase
                        .builder()
                        .orderManager(uk.gov.justice.digital.delius.data.api.OrderManager
                                .builder()
                                .build())
                        .offences(ImmutableList.of(uk.gov.justice.digital.delius.data.api.Offence
                                .builder()
                                .detail(OffenceDetail
                                        .builder()
                                        .build())
                                .mainOffence(true)
                                .build()))
                        .courtAppearance(uk.gov.justice.digital.delius.data.api.CourtAppearance
                                .builder()
                                .court(uk.gov.justice.digital.delius.data.api.Court
                                        .builder()
                                        .build())
                                .build())
                        .build());

        assertThat(conviction).isNotNull();
        assertThat(conviction.getIndex()).isEqualTo("3");

        verify(spgNotificationService).notifyNewCourtCaseCreated(any(Event.class));
    }

    @Nested
    class GetConvictionsWithRar {

        @Test
        public void convictionsReturnedIgnoresSoftDeletedEvents() {
            when(eventRepository.findByOffenderIdAndActiveFlagTrue(1L))
                .thenReturn(asList(
                    aEvent().toBuilder().eventId(99L).offenderId(1L).softDeleted(true).build()
                ));

            assertThat(convictionService.convictionsWithActiveRequirementFor(1L, REHABILITATION_ACTIVITY_REQUIREMENT_CODE))
                .isEmpty();
        }

        @Test
        public void convictionsReturnedFiltersEventsWithNoDisposals() {
            when(eventRepository.findByOffenderIdAndActiveFlagTrue(1L))
                .thenReturn(asList(
                    aEvent().toBuilder().eventId(99L).offenderId(1L).softDeleted(false).build()
                ));

            assertThat(convictionService.convictionsWithActiveRequirementFor(1L, REHABILITATION_ACTIVITY_REQUIREMENT_CODE))
                .isEmpty();
        }

        @Test
        public void convictionsReturnedFiltersEventsWithNoRequirements() {
            when(eventRepository.findByOffenderIdAndActiveFlagTrue(1L))
                .thenReturn(asList(
                    aEvent().toBuilder().eventId(99L).offenderId(1L).softDeleted(false)
                        .disposal(Disposal.builder().disposalId(98L).requirements(emptyList()).build())
                        .build()
                ));

            assertThat(convictionService.convictionsWithActiveRequirementFor(1L, REHABILITATION_ACTIVITY_REQUIREMENT_CODE))
                .isEmpty();
        }

        @Test
        public void convictionsReturnedFiltersEventsWithNoActiveRequirements() {
            when(eventRepository.findByOffenderIdAndActiveFlagTrue(1L))
                .thenReturn(asList(
                    aEvent().toBuilder().eventId(99L).offenderId(1L).softDeleted(false)
                        .disposal(Disposal.builder().disposalId(98L).requirements(
                            singletonList(Requirement.builder().activeFlag(false).build())).build())
                        .build()
                ));

            assertThat(convictionService.convictionsWithActiveRequirementFor(1L, REHABILITATION_ACTIVITY_REQUIREMENT_CODE))
                .isEmpty();
        }

        @Test
        public void convictionsReturnedFiltersEventsWithNoRarRequirements() {
            when(eventRepository.findByOffenderIdAndActiveFlagTrue(1L))
                .thenReturn(asList(
                    aEvent().toBuilder().eventId(99L).offenderId(1L).softDeleted(false)
                        .disposal(Disposal.builder().disposalId(98L).requirements(
                            singletonList(Requirement.builder().activeFlag(false).requirementTypeMainCategory(
                                RequirementTypeMainCategory.builder().code(EXCLUSION_REQUIREMENT_CODE).build()).build())).build())
                        .build()
                ));

            assertThat(convictionService.convictionsWithActiveRequirementFor(1L, REHABILITATION_ACTIVITY_REQUIREMENT_CODE))
                .isEmpty();
        }

        @Test
        public void convictionsReturnedOrderedByReferralDateReversed() {
            when(eventRepository.findByOffenderIdAndActiveFlagTrue(1L))
                .thenReturn(asList(
                    aEvent().toBuilder().eventId(99L).offenderId(1L).referralDate(now()).softDeleted(false)
                        .disposal(Disposal.builder().disposalId(98L).disposalType(aNcDisposalType()).requirements(
                            singletonList(Requirement.builder().activeFlag(true).requirementTypeMainCategory(
                                RequirementTypeMainCategory.builder().code(REHABILITATION_ACTIVITY_REQUIREMENT_CODE).build()).build())).build())
                        .build(),
                    aEvent().toBuilder().eventId(101L).offenderId(1L).referralDate(now().plusDays(1)).softDeleted(false)
                        .disposal(Disposal.builder().disposalId(100L).disposalType(aNcDisposalType()).requirements(
                            singletonList(Requirement.builder().activeFlag(true).requirementTypeMainCategory(
                                RequirementTypeMainCategory.builder().code(REHABILITATION_ACTIVITY_REQUIREMENT_CODE).build()).build())).build())
                        .build()
                ));

            List<Conviction> response = convictionService.convictionsWithActiveRequirementFor(1L, REHABILITATION_ACTIVITY_REQUIREMENT_CODE);
            assertThat(response.size()).isEqualTo(2);
            assertThat(response.get(0).getReferralDate()).isEqualTo(now().plusDays(1));
            assertThat(response.get(1).getReferralDate()).isEqualTo(now().plusDays(0));
        }
    }

    @Nested
    class GetByBookingNumber {
        @Test
        public void convictionReturnedWhenSingleConvictionMatchedForPrisonBookingNumber() throws DuplicateActiveCustodialConvictionsException {
            when(eventRepository.findByPrisonBookingNumber("A12345")).thenReturn(ImmutableList.of(
                    aEvent()
                            .toBuilder()
                            .eventId(999L)
                            .build()));

            Optional<Long> maybeConviction = convictionService.getConvictionIdByPrisonBookingNumber("A12345");

            assertThat(maybeConviction)
                    .get()
                    .isEqualTo(999L);

        }
        @Test
        public void exceptionThrownWhenMultipleActiveConvictionsMatchedForPrisonBookingNumber() {
            when(eventRepository.findByPrisonBookingNumber("A12345")).thenReturn(ImmutableList.of(
                    aEvent()
                            .toBuilder()
                            .eventId(999L)
                            .build(),
                    aEvent()
                            .toBuilder()
                            .eventId(998L)
                            .build())
            );

            assertThatThrownBy(
                    () -> convictionService.getConvictionIdByPrisonBookingNumber("A12345"))
                    .isInstanceOf(DuplicateActiveCustodialConvictionsException.class);
        }
        @Test
        public void convictionReturnedWhenSingleActiveConvictionMatchedAmoungstDuplicatesForPrisonBookingNumber() throws DuplicateActiveCustodialConvictionsException {
            when(eventRepository.findByPrisonBookingNumber("A12345")).thenReturn(ImmutableList.of(
                    aEvent()
                            .toBuilder()
                            .eventId(998L)
                            .activeFlag(false)
                            .build(),
                    aEvent()
                            .toBuilder()
                            .eventId(999L)
                            .activeFlag(true)
                            .build()
            ));

            Optional<Long> maybeConviction = convictionService.getConvictionIdByPrisonBookingNumber("A12345");

            assertThat(maybeConviction)
                    .get()
                    .isEqualTo(999L);

        }
        @Test
        public void emptyReturnedWhenNoConvictionsMatchedForPrisonBookingNumber() throws DuplicateActiveCustodialConvictionsException {
            when(eventRepository.findByPrisonBookingNumber("A12345")).thenReturn(ImmutableList.of());

            Optional<Long> maybeConviction = convictionService.getConvictionIdByPrisonBookingNumber("A12345");

            assertThat(maybeConviction).isNotPresent();
        }

    }

    @Nested
    class GetSingleActiveByBookingNumber {
        @Test
        @DisplayName("will match a single custodial sentence")
        public void convictionReturnedWhenSingleConvictionMatchedForOffenderIdPrisonBookingNumber() throws DuplicateActiveCustodialConvictionsException {
            when(eventRepository.findActiveByOffenderIdWithCustody(99L)).thenReturn(ImmutableList.of(
                    aCustodialEvent("A12345", true)
                            .toBuilder()
                            .eventId(999L)
                            .build()));

            Optional<Event> maybeConviction = convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(99L, "A12345");

            assertThat(maybeConviction).isPresent();
        }
        @Test
        @DisplayName("will not match a custodial sentence in Post Sentence Supervision phase")
        public void custodialSentenceIsPSSIgnored() throws DuplicateActiveCustodialConvictionsException {
            when(eventRepository.findActiveByOffenderIdWithCustody(99L)).thenReturn(ImmutableList.of(
                    aCustodialEvent("A12345", true, "P")
                            .toBuilder()
                            .eventId(999L)
                            .build()));

            Optional<Event> maybeConviction = convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(99L, "A12345");

            assertThat(maybeConviction).isEmpty();
        }
        @Test
        @DisplayName("will throw exception is multiple active convictions matched with same prison book number")
        public void exceptionThrownWhenMultipleActiveConvictionsMatchedForOffenderIdPrisonBookingNumber() {
            when(eventRepository.findActiveByOffenderIdWithCustody(99L)).thenReturn(ImmutableList.of(
                    aCustodialEvent("A12345", true)
                            .toBuilder()
                            .eventId(999L)
                            .build(),
                    aCustodialEvent("A12345", true)
                            .toBuilder()
                            .eventId(998L)
                            .build())
            );

            assertThatThrownBy(
                    () -> convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(99L, "A12345"))
                    .isInstanceOf(DuplicateActiveCustodialConvictionsException.class);
        }

        @Test
        @DisplayName("will throw exception is multiple active custodial convictions matched even when prison book number not set")
        public void exceptionThrownWhenMultipleActiveCustodialConvictionsMatchedForOffenderId() {
            when(eventRepository.findActiveByOffenderIdWithCustody(99L)).thenReturn(ImmutableList.of(
                    aCustodialEvent("A12345", true)
                            .toBuilder()
                            .eventId(999L)
                            .build(),
                    aCustodialEvent(null, true)
                            .toBuilder()
                            .eventId(998L)
                            .build())
            );

            assertThatThrownBy(
                    () -> convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(99L, "A12345"))
                    .isInstanceOf(DuplicateActiveCustodialConvictionsException.class);
        }
        @Test
        @DisplayName("will match a single custodial sentence when others are in the Post Sentence Supervision phase")
        public void convictionReturnedWhenMultiplesButOneInPSSPhase() throws DuplicateActiveCustodialConvictionsException {
            when(eventRepository.findActiveByOffenderIdWithCustody(99L)).thenReturn(ImmutableList.of(
                    aCustodialEvent("A12345", true, "P") // Post Sentence Supervision
                            .toBuilder()
                            .eventId(998L)
                            .build(),
                    aCustodialEvent("A12345", true, "D") // in custody
                            .toBuilder()
                            .eventId(999L)
                            .build()
            ));

            Optional<Event> maybeConviction = convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(99L, "A12345");

            assertThat(maybeConviction.orElseThrow().getEventId())
                    .isEqualTo(999L);

        }
        @Test
        @DisplayName("nothing returned if no matching convictions found")
        public void emptyReturnedWhenNoConvictionsMatchedForOffenderIdAndPrisonBookingNumber() throws DuplicateActiveCustodialConvictionsException {
            when(eventRepository.findActiveByOffenderIdWithCustody(99L)).thenReturn(ImmutableList.of());

            Optional<Event> maybeConviction = convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(99L, "A12345");

            assertThat(maybeConviction).isNotPresent();
        }

        @Test
        @DisplayName("nothing returned if no matching convictions found with correct prison book number")
        public void emptyReturnedWhenSingleConvictionMatchedForOffenderIdButNotPrisonBookingNumber() throws DuplicateActiveCustodialConvictionsException {
            when(eventRepository.findActiveByOffenderIdWithCustody(99L)).thenReturn(ImmutableList.of(
                    aCustodialEvent("X65432", true)
                            .toBuilder()
                            .eventId(999L)
                            .build()));

            Optional<Event> maybeConviction = convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(99L, "A12345");

            assertThat(maybeConviction).isEmpty();
        }

        @Test
        @DisplayName("will return ID of match a single custodial sentence when others are inactive")
        public void convictionIdReturnedWhenSingleActiveConvictionMatchedAmongstDuplicatesForOffenderIdPrisonBookingNumber() throws DuplicateActiveCustodialConvictionsException {
            when(eventRepository.findActiveByOffenderIdWithCustody(99L)).thenReturn(ImmutableList.of(
                    aCustodialEvent("A12345", true)
                            .toBuilder()
                            .eventId(999L)
                            .activeFlag(true)
                            .build()
            ));

            Optional<Long> maybeConviction = convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(99L, "A12345");

            assertThat(maybeConviction.orElseThrow())
                    .isEqualTo(999L);

        }
    }
    @Nested
    class GetSingleActiveCloseToSentenceDate {
        @Test
        @DisplayName("will return active custodial conviction with the same sentence start date")
        public void convictionReturnedWhenSingleConvictionMatchedForOffenderIdAndSentenceDate() {
            when(eventRepository.findActiveByOffenderIdWithCustody(99L)).thenReturn(ImmutableList.of(
                    anActiveCustodialEvent(LocalDate.of(2020, 1, 30))
                            .toBuilder()
                            .eventId(999L)
                            .build()));

            final var maybeConviction = convictionService.getSingleActiveConvictionIdByOffenderIdAndCloseToSentenceDate(99L, LocalDate.of(2020, 1, 30));

            assertThat(maybeConviction.get()).isPresent();
        }

        @Test
        @DisplayName("will return active custodial conviction with the sentence start date within 7 days")
        public void convictionReturnedWhenSingleConvictionMatchedForOffenderIdAndCloseToSentenceDate() {
            when(eventRepository.findActiveByOffenderIdWithCustody(99L)).thenReturn(ImmutableList.of(
                    anActiveCustodialEvent(LocalDate.of(2020, 1, 30))
                            .toBuilder()
                            .eventId(999L)
                            .build()));

            final var maybeConviction = convictionService.getSingleActiveConvictionIdByOffenderIdAndCloseToSentenceDate(99L, LocalDate.of(2020, 1, 23));

            assertThat(maybeConviction.get()).isPresent();
        }
        @Test
        @DisplayName("will return nothing when active custodial conviction has sentence start date after 7 days")
        public void convictionNotReturnedWhenSingleConvictionMatchedForOffenderIdButNotCloseToSentenceDateAfter() {
            when(eventRepository.findActiveByOffenderIdWithCustody(99L)).thenReturn(ImmutableList.of(
                    anActiveCustodialEvent(LocalDate.of(2020, 1, 30))
                            .toBuilder()
                            .eventId(999L)
                            .build()));

            final var maybeConviction = convictionService.getSingleActiveConvictionIdByOffenderIdAndCloseToSentenceDate(99L, LocalDate.of(2020, 1, 21));

            assertThat(maybeConviction.get()).isEmpty();
        }
        @Test
        @DisplayName("will return nothing when active custodial conviction has sentence start date before 7 days")
        public void convictionNotReturnedWhenSingleConvictionMatchedForOffenderIdButNotCloseToSentenceDateBefore() {
            when(eventRepository.findActiveByOffenderIdWithCustody(99L)).thenReturn(ImmutableList.of(
                    anActiveCustodialEvent(LocalDate.of(2020, 1, 21))
                            .toBuilder()
                            .eventId(999L)
                            .build()));

            final var maybeConviction = convictionService.getSingleActiveConvictionIdByOffenderIdAndCloseToSentenceDate(99L, LocalDate.of(2020, 1, 30));

            assertThat(maybeConviction.get()).isEmpty();
        }
        @Test
        @DisplayName("exception thrown if more than one active custodial conviction found close to sentence start date")
        public void exceptionThrownWhenMultipleActiveConvictionsMatchedForOffenderIdAndSentenceDate() {
            when(eventRepository.findActiveByOffenderIdWithCustody(99L)).thenReturn(ImmutableList.of(
                    anActiveCustodialEvent(LocalDate.of(2020, 1, 30))
                            .toBuilder()
                            .eventId(999L)
                            .build(),
                    anActiveCustodialEvent(LocalDate.of(2020, 1, 30))
                            .toBuilder()
                            .eventId(998L)
                            .build())
            );

            assertThatThrownBy(
                    () -> { throw convictionService.getSingleActiveConvictionIdByOffenderIdAndCloseToSentenceDate(99L, LocalDate.of(2020, 1, 30)).getError(); })
                    .isInstanceOf(ConvictionService.DuplicateConvictionsForSentenceDateException.class);
        }
        @Test
        @DisplayName("will return the conviction not in Post Sentence Supervision phase if more than one active custodial conviction found close to sentence start date")
        public void willIgnorePSSConvictionsWHenMultipleCustodialEventsFoundThatMatchDate() {
            when(eventRepository.findActiveByOffenderIdWithCustody(99L)).thenReturn(ImmutableList.of(
                    anActiveCustodialEvent(LocalDate.of(2020, 1, 30), "D")
                            .toBuilder()
                            .eventId(999L)
                            .build(),
                    anActiveCustodialEvent(LocalDate.of(2020, 1, 30), "P")
                            .toBuilder()
                            .eventId(998L)
                            .build())
            );

            final var maybeConviction = convictionService.getSingleActiveConvictionIdByOffenderIdAndCloseToSentenceDate(99L, LocalDate.of(2020, 1, 30));

            assertThat(maybeConviction.get().orElseThrow().getEventId())
                    .isEqualTo(999L);
        }

        @Test
        @DisplayName("will return nothing when no convictions")
        public void emptyReturnedWhenNoConvictionsMatchedForOffenderIdAndSentenceDate() {
            when(eventRepository.findActiveByOffenderIdWithCustody(99L)).thenReturn(ImmutableList.of());

            final var maybeConviction = convictionService.getSingleActiveConvictionIdByOffenderIdAndCloseToSentenceDate(99L, LocalDate.of(2020, 1, 30));

            assertThat(maybeConviction.get()).isNotPresent();
        }

        @Test
        @DisplayName("will return nothing if conviction is matched but is in Post Sentence Supervision Phase")
        public void emptyReturnedWhenNoConvictionsMatchedForOffenderIdAndSentenceDateThatIsNotPSS() {
            when(eventRepository.findActiveByOffenderIdWithCustody(99L)).thenReturn(ImmutableList.of(
                    anActiveCustodialEvent(LocalDate.of(2020, 1, 30), "P")
                            .toBuilder()
                            .eventId(999L)
                            .build()));

            final var maybeConviction = convictionService.getSingleActiveConvictionIdByOffenderIdAndCloseToSentenceDate(99L, LocalDate.of(2020, 1, 30));

            assertThat(maybeConviction.get()).isEmpty();
        }

    }

    @Nested
    class GetActiveCustodialEvent {
        @Test
        @DisplayName("will return and active custodial event")
        public void getActiveCustodialEvent_singleActiveEvent_returnsEvent() throws ConvictionService.SingleActiveCustodyConvictionNotFoundException {
            final var expectedEvent = anActiveCustodialEvent();
            when(eventRepository.findActiveByOffenderIdWithCustody(ANY_OFFENDER_ID)).thenReturn(List.of(expectedEvent));

            final var actualEvent = convictionService.getActiveCustodialEvent(ANY_OFFENDER_ID);

            assertThat(actualEvent).isEqualTo(expectedEvent);
        }

        @Test
        @DisplayName("will throw exception when no events found")
        public void getActiveCustodialEvent_noEvents_throwsException() throws ConvictionService.SingleActiveCustodyConvictionNotFoundException {
            when(eventRepository.findActiveByOffenderIdWithCustody(ANY_OFFENDER_ID)).thenReturn(emptyList());

            assertThatThrownBy(() ->
                    convictionService.getActiveCustodialEvent(ANY_OFFENDER_ID)
            ).isInstanceOf(ConvictionService.SingleActiveCustodyConvictionNotFoundException.class);
        }

        @Test
        @DisplayName("will throw exception when only custodial event found is in Post Sentence Supervision phase")
        public void getActiveCustodialEvent_onlyPSSEvent_throwsException() throws ConvictionService.SingleActiveCustodyConvictionNotFoundException {
            when(eventRepository.findActiveByOffenderIdWithCustody(ANY_OFFENDER_ID)).thenReturn(List.of(anActiveCustodialEvent("P")));

            assertThatThrownBy(() ->
                    convictionService.getActiveCustodialEvent(ANY_OFFENDER_ID)
            ).isInstanceOf(ConvictionService.SingleActiveCustodyConvictionNotFoundException.class);
        }

        @Test
        public void getActiveCustodialEvent_multipleEvents_throwsException() throws ConvictionService.SingleActiveCustodyConvictionNotFoundException {
            when(eventRepository.findActiveByOffenderIdWithCustody(ANY_OFFENDER_ID)).thenReturn(List.of(anActiveCustodialEvent(), anActiveCustodialEvent()));

            assertThatThrownBy(() ->
                    convictionService.getActiveCustodialEvent(ANY_OFFENDER_ID)
            ).isInstanceOf(ConvictionService.SingleActiveCustodyConvictionNotFoundException.class);
        }

        @Test
        public void getActiveCustodialEvent_multipleActiveEventOneWithPSS_returnsEvent() throws ConvictionService.SingleActiveCustodyConvictionNotFoundException {
            final var expectedEvent = anActiveCustodialEvent("D");
            final var postSentenceSupervisionEvent = anActiveCustodialEvent("P");
            when(eventRepository.findActiveByOffenderIdWithCustody(ANY_OFFENDER_ID)).thenReturn(List.of(expectedEvent, postSentenceSupervisionEvent));

            final var actualEvent = convictionService.getActiveCustodialEvent(ANY_OFFENDER_ID);

            assertThat(actualEvent).isEqualTo(expectedEvent);
        }


    }

    @Nested
    class GetAllActiveCustodialEventsWithBookingNumber {
        @Test
        @DisplayName("will return all matching with bookNumber")
        void willReturnAllMatchingWithBookNumber() {
            when(eventRepository.findActiveByOffenderIdWithCustody(ANY_OFFENDER_ID)).thenReturn(List.of(anActiveCustodialEventWithBookNumber("12345T"),
                anActiveCustodialEventWithBookNumber("12345T"),
                anActiveCustodialEventWithBookNumber("99999T")));

            assertThat(convictionService.getAllActiveCustodialEventsWithBookingNumber(ANY_OFFENDER_ID, "12345T")).hasSize(2);
            assertThat(convictionService.getAllActiveCustodialEventsWithBookingNumber(ANY_OFFENDER_ID, "99999T")).hasSize(1);
        }
    }

    private Event aEvent() {
        return Event.builder()
                .referralDate(now())
                .additionalOffences(ImmutableList.of())
                .activeFlag(true)
                .build();
    }

    private Event aCustodialEvent(String prisonerNumber, boolean active) {
        return aCustodialEvent(prisonerNumber, active, "D");
    }
    private Event aCustodialEvent(String prisonerNumber, boolean active, String custodialStatus) {
        Disposal disposal = Disposal.builder()
                .terminationDate(null)
                .disposalType(aNcDisposalType())
                .custody(Custody.builder().prisonerNumber(prisonerNumber).custodialStatus(StandardReference.builder().codeValue(custodialStatus).build()).build())
                .build();
        return Event.builder()
                .softDeleted(false)
                .activeFlag(active)
                .disposal(disposal)
                .build();
    }

    private DisposalType aNcDisposalType() {
        return DisposalType.builder().sentenceType(NC_SENTENCE_TYPE).build();
    }

    private Event anActiveCustodialEvent() {
        return anActiveCustodialEvent("D");
    }

    private Event anActiveCustodialEventWithBookNumber(String bookNumber) {
        final var event = anActiveCustodialEvent("D");
        final var custody = event.getDisposal().getCustody().toBuilder().prisonerNumber(bookNumber).build();
        final var disposal = event.getDisposal().toBuilder().custody(custody).build();
        return event.toBuilder().disposal(disposal).build();
    }

    private Event anActiveCustodialEvent(String custodialStatus) {
        Disposal disposal = Disposal.builder()
                .terminationDate(null)
                .disposalType(aNcDisposalType())
                .custody(Custody.builder().custodialStatus(StandardReference.builder().codeValue(custodialStatus).build()).build())
                .build();
        return Event.builder()
                .softDeleted(false)
                .activeFlag(true)
                .disposal(disposal)
                .build();
    }
    private Event anActiveCustodialEvent(LocalDate sentenceStartDate) {
        return anActiveCustodialEvent(sentenceStartDate, "D");
    }
    private Event anActiveCustodialEvent(LocalDate sentenceStartDate, String custodialStatus) {
        Disposal disposal = Disposal.builder()
                .terminationDate(null)
                .disposalType(aNcDisposalType())
                .startDate(sentenceStartDate)
                .custody(Custody.builder().custodialStatus(StandardReference.builder().codeValue(custodialStatus).build()).build())
                .build();
        return Event.builder()
                .softDeleted(false)
                .activeFlag(true)
                .disposal(disposal)
                .build();
    }

}
