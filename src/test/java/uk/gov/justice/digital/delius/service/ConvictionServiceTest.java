package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.CourtCase;
import uk.gov.justice.digital.delius.data.api.OffenceDetail;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.service.ConvictionService.DuplicateConvictionsForBookingNumberException;
import uk.gov.justice.digital.delius.transformers.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import({ConvictionService.class, ConvictionTransformer.class, MainOffenceTransformer.class, AdditionalOffenceTransformer.class, CourtAppearanceTransformer.class, CourtReportTransformer.class, CourtTransformer.class, InstitutionTransformer.class, CustodyKeyDateTransformer.class})
public class ConvictionServiceTest {

    private static final Long ANY_OFFENDER_ID = 123L;

    @Autowired
    private ConvictionService convictionService;

    @MockBean
    private EventRepository convictionRepository;

    @MockBean
    private LookupSupplier lookupSupplier;

    @MockBean
    private SpgNotificationService spgNotificationService;

    @MockBean
    private IAPSNotificationService iapsNotificationService;

    @Test
    public void convictionsOrderedByCreationDate() {
        Mockito.when(convictionRepository.findByOffenderId(1L))
                .thenReturn(ImmutableList.of(
                        aEvent().toBuilder().eventId(99L).referralDate(LocalDate.now().minusDays(1)).build(),
                        aEvent().toBuilder().eventId(9L).referralDate(LocalDate.now().minusDays(2)).build(),
                        aEvent().toBuilder().eventId(999L).referralDate(LocalDate.now()).build()
                ));

        assertThat(convictionService.convictionsFor(1L)
                .stream().map(uk.gov.justice.digital.delius.data.api.Conviction::getConvictionId)
                .collect(Collectors.toList()))
                .containsSequence(999L, 99L, 9L);

    }

    @Test
    public void deletedRecordsIgnored() {
        Mockito.when(convictionRepository.findByOffenderId(1L))
                .thenReturn(ImmutableList.of(
                        aEvent().toBuilder().eventId(1L).build(),
                        aEvent().toBuilder().eventId(2L).softDeleted(1L).build(),
                        aEvent().toBuilder().eventId(3L).build()
                ));

        assertThat(convictionService.convictionsFor(1L)
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
        when(convictionRepository.save(any(Event.class))).thenAnswer((invocationOnMock -> invocationOnMock.getArgument(0)));
        when(convictionRepository.findByOffenderId(1L)).thenReturn(ImmutableList.of(aEvent(), aEvent()));

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
    class GetByBookingNumber {
        @Test
        public void convictionReturnedWhenSingleConvictionMatchedForPrisonBookingNumber() throws DuplicateConvictionsForBookingNumberException {
            when(convictionRepository.findByPrisonBookingNumber("A12345")).thenReturn(ImmutableList.of(
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
            when(convictionRepository.findByPrisonBookingNumber("A12345")).thenReturn(ImmutableList.of(
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
                    .isInstanceOf(DuplicateConvictionsForBookingNumberException.class);
        }
        @Test
        public void convictionReturnedWhenSingleActiveConvictionMatchedAmoungstDuplicatesForPrisonBookingNumber() throws DuplicateConvictionsForBookingNumberException {
            when(convictionRepository.findByPrisonBookingNumber("A12345")).thenReturn(ImmutableList.of(
                    aEvent()
                            .toBuilder()
                            .eventId(998L)
                            .activeFlag(0L)
                            .build(),
                    aEvent()
                            .toBuilder()
                            .eventId(999L)
                            .activeFlag(1L)
                            .build()
            ));

            Optional<Long> maybeConviction = convictionService.getConvictionIdByPrisonBookingNumber("A12345");

            assertThat(maybeConviction)
                    .get()
                    .isEqualTo(999L);

        }
        @Test
        public void emptyReturnedWhenNoConvictionsMatchedForPrisonBookingNumber() throws DuplicateConvictionsForBookingNumberException {
            when(convictionRepository.findByPrisonBookingNumber("A12345")).thenReturn(ImmutableList.of());

            Optional<Long> maybeConviction = convictionService.getConvictionIdByPrisonBookingNumber("A12345");

            assertThat(maybeConviction).isNotPresent();
        }

    }

    @Nested
    class GetSingleActiveByBookingNumber {
        @Test
        public void convictionReturnedWhenSingleConvictionMatchedForOffenderIdPrisonBookingNumber() throws DuplicateConvictionsForBookingNumberException {
            when(convictionRepository.findByOffenderIdAndPrisonBookingNumber(99L, "A12345")).thenReturn(ImmutableList.of(
                    aEvent()
                            .toBuilder()
                            .eventId(999L)
                            .build()));

            Optional<Event> maybeConviction = convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(99L, "A12345");

            assertThat(maybeConviction).isPresent();
        }
        @Test
        public void exceptionThrownWhenMultipleActiveConvictionsMatchedForOffenderIdPrisonBookingNumber() {
            when(convictionRepository.findByOffenderIdAndPrisonBookingNumber(99L, "A12345")).thenReturn(ImmutableList.of(
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
                    () -> convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(99L, "A12345"))
                    .isInstanceOf(DuplicateConvictionsForBookingNumberException.class);
        }
        @Test
        public void convictionReturnedWhenSingleActiveConvictionMatchedAmoungstDuplicatesForOffenderIdPrisonBookingNumber() throws DuplicateConvictionsForBookingNumberException {
            when(convictionRepository.findByOffenderIdAndPrisonBookingNumber(99L, "A12345")).thenReturn(ImmutableList.of(
                    aEvent()
                            .toBuilder()
                            .eventId(998L)
                            .activeFlag(0L)
                            .build(),
                    aEvent()
                            .toBuilder()
                            .eventId(999L)
                            .activeFlag(1L)
                            .build()
            ));

            Optional<Event> maybeConviction = convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(99L, "A12345");

            assertThat(maybeConviction.orElseThrow().getEventId())
                    .isEqualTo(999L);

        }
        @Test
        public void emptyReturnedWhenNoConvictionsMatchedForOffenderIdAndPrisonBookingNumber() throws DuplicateConvictionsForBookingNumberException {
            when(convictionRepository.findByOffenderIdAndPrisonBookingNumber(99L, "A12345")).thenReturn(ImmutableList.of());

            Optional<Event> maybeConviction = convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(99L, "A12345");

            assertThat(maybeConviction).isNotPresent();
        }

    }
    @Nested
    class GetSingleActiveCloseToSentenceData {
        @Test
        public void convictionReturnedWhenSingleConvictionMatchedForOffenderIdAndSentenceDate() {
            when(convictionRepository.findByOffenderIdWithCustody(99L)).thenReturn(ImmutableList.of(
                    anActiveEvent(LocalDate.of(2020, 1, 30))
                            .toBuilder()
                            .eventId(999L)
                            .build()));

            final var maybeConviction = convictionService.getSingleActiveConvictionIdByOffenderIdAndCloseToSentenceDate(99L, LocalDate.of(2020, 1, 30));

            assertThat(maybeConviction.get()).isPresent();
        }
        @Test
        public void convictionReturnedWhenSingleConvictionMatchedForOffenderIdAndCloseToSentenceDate() {
            when(convictionRepository.findByOffenderIdWithCustody(99L)).thenReturn(ImmutableList.of(
                    anActiveEvent(LocalDate.of(2020, 1, 30))
                            .toBuilder()
                            .eventId(999L)
                            .build()));

            final var maybeConviction = convictionService.getSingleActiveConvictionIdByOffenderIdAndCloseToSentenceDate(99L, LocalDate.of(2020, 1, 23));

            assertThat(maybeConviction.get()).isPresent();
        }
        @Test
        public void convictionReturnedWhenSingleConvictionMatchedForOffenderIdButNotCloseToSentenceDate() {
            when(convictionRepository.findByOffenderIdWithCustody(99L)).thenReturn(ImmutableList.of(
                    anActiveEvent(LocalDate.of(2020, 1, 30))
                            .toBuilder()
                            .eventId(999L)
                            .build()));

            final var maybeConviction = convictionService.getSingleActiveConvictionIdByOffenderIdAndCloseToSentenceDate(99L, LocalDate.of(2020, 1, 21));

            assertThat(maybeConviction.get()).isPresent();
        }
        @Test
        public void exceptionThrownWhenMultipleActiveConvictionsMatchedForOffenderIdAndSentenceDate() {
            when(convictionRepository.findByOffenderIdWithCustody(99L)).thenReturn(ImmutableList.of(
                    anActiveEvent(LocalDate.of(2020, 1, 30))
                            .toBuilder()
                            .eventId(999L)
                            .build(),
                    anActiveEvent(LocalDate.of(2020, 1, 30))
                            .toBuilder()
                            .eventId(998L)
                            .build())
            );

            assertThatThrownBy(
                    () -> convictionService.getSingleActiveConvictionIdByOffenderIdAndCloseToSentenceDate(99L, LocalDate.of(2020, 1, 30)))
                    .isInstanceOf(ConvictionService.DuplicateConvictionsForSentenceDateException.class);
        }

        @Test
        public void convictionReturnedWhenSingleActiveConvictionMatchedAmoungstDuplicatesForOffenderIdAndSentenceDate() throws ConvictionService.DuplicateConvictionsForSentenceDateException {
            when(convictionRepository.findByOffenderIdWithCustody(99L)).thenReturn(ImmutableList.of(
                    anActiveEvent(LocalDate.of(2020, 1, 30))
                            .toBuilder()
                            .eventId(998L)
                            .activeFlag(0L)
                            .build(),
                    anActiveEvent(LocalDate.of(2020, 1, 30))
                            .toBuilder()
                            .eventId(999L)
                            .activeFlag(1L)
                            .build()
            ));

            final var maybeConviction = convictionService.getSingleActiveConvictionIdByOffenderIdAndCloseToSentenceDate(99L, LocalDate.of(2020, 1, 30));

            assertThat(maybeConviction.get().orElseThrow().getEventId())
                    .isEqualTo(999L);

        }
        @Test
        public void emptyReturnedWhenNoConvictionsMatchedForOffenderIdAndSentenceDate() throws ConvictionService.DuplicateConvictionsForSentenceDateException {
            when(convictionRepository.findByOffenderIdWithCustody(99L)).thenReturn(ImmutableList.of());

            final var maybeConviction = convictionService.getSingleActiveConvictionIdByOffenderIdAndCloseToSentenceDate(99L, LocalDate.of(2020, 1, 30));

            assertThat(maybeConviction.get()).isNotPresent();
        }

    }

    @Nested
    class GetActiveCustodialEvent {
        @Test
        public void getActiveCustodialEvent_singleActiveEvent_returnsEvent() throws ConvictionService.SingleActiveCustodyConvictionNotFoundException {
            final var expectedEvent = anActiveEvent();
            when(convictionRepository.findByOffenderId(ANY_OFFENDER_ID)).thenReturn(List.of(expectedEvent));

            final var actualEvent = convictionService.getActiveCustodialEvent(ANY_OFFENDER_ID);

            assertThat(actualEvent).isEqualTo(expectedEvent);
        }

        @Test
        public void getActiveCustodialEvent_noEvents_throwsException() throws ConvictionService.SingleActiveCustodyConvictionNotFoundException {
            when(convictionRepository.findByOffenderId(ANY_OFFENDER_ID)).thenReturn(Collections.emptyList());

            assertThatThrownBy(() ->
                    convictionService.getActiveCustodialEvent(ANY_OFFENDER_ID)
            ).isInstanceOf(ConvictionService.SingleActiveCustodyConvictionNotFoundException.class);
        }

        @Test
        public void getActiveCustodialEvent_multipleEvents_throwsException() throws ConvictionService.SingleActiveCustodyConvictionNotFoundException {
            when(convictionRepository.findByOffenderId(ANY_OFFENDER_ID)).thenReturn(List.of(anActiveEvent(), anActiveEvent()));

            assertThatThrownBy(() ->
                    convictionService.getActiveCustodialEvent(ANY_OFFENDER_ID)
            ).isInstanceOf(ConvictionService.SingleActiveCustodyConvictionNotFoundException.class);
        }
    }

    private Event aEvent() {
        return Event.builder()
                .referralDate(LocalDate.now())
                .additionalOffences(ImmutableList.of())
                .activeFlag(1L)
                .build();
    }

    private Event anActiveEvent() {
        Disposal disposal = Disposal.builder()
                .terminationDate(null)
                .disposalType(DisposalType.builder().sentenceType("NC").build())
                .custody(Custody.builder().build())
                .build();
        return Event.builder()
                .softDeleted(0L)
                .activeFlag(1L)
                .disposal(disposal)
                .build();
    }

    private Event anActiveEvent(LocalDate sentenceStartDate) {
        Disposal disposal = Disposal.builder()
                .terminationDate(null)
                .disposalType(DisposalType.builder().sentenceType("NC").build())
                .startDate(sentenceStartDate)
                .custody(Custody.builder().build())
                .build();
        return Event.builder()
                .softDeleted(0L)
                .activeFlag(1L)
                .disposal(disposal)
                .build();
    }

}
