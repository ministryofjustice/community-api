package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.CourtCase;
import uk.gov.justice.digital.delius.data.api.UnpaidWork;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.MainOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offence;
import uk.gov.justice.digital.delius.jpa.standard.entity.RInstitution;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.entity.UpwAppointment;
import uk.gov.justice.digital.delius.jpa.standard.entity.UpwDetails;
import uk.gov.justice.digital.delius.util.EntityHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.util.EntityHelper.aKeyDate;

public class ConvictionTransformerTest {
    @Test
    public void convictionIdMappedFromEventId() {
        assertThat(ConvictionTransformer.convictionOf(
                anEvent()
                    .toBuilder()
                    .eventId(99L)
                    .build()).getConvictionId()
        ).isEqualTo(99L);

    }

    @Test
    public void offencesCollatedFromMainAndAdditionalOffences() {

        assertThat(ConvictionTransformer.convictionOf(
                anEvent()
                        .toBuilder()
                        .eventId(99L)
                        .mainOffence(aMainOffence())
                        .additionalOffences(ImmutableList.of(anAdditionalOffence(), anAdditionalOffence()))
                        .build()).getOffences()
        ).hasSize(3);
    }

    @Test
    public void activeMappedForZeroOneActiveFlag() {
        assertThat((ConvictionTransformer.convictionOf(anEvent().toBuilder().activeFlag(1L).build()).getActive())).isTrue();
        assertThat((ConvictionTransformer.convictionOf(anEvent().toBuilder().activeFlag(0L).build()).getActive())).isFalse();

    }

    @Test
    public void inBreachMappedForZeroOneInBreach() {
        assertThat((ConvictionTransformer.convictionOf(anEvent().toBuilder().inBreach(1L).build()).getInBreach())).isTrue();
        assertThat((ConvictionTransformer.convictionOf(anEvent().toBuilder().inBreach(0L).build()).getInBreach())).isFalse();

    }

    @Test
    public void sentenceIsMappedWhenEvenHasDisposal() {
        assertThat((ConvictionTransformer.convictionOf(anEvent().toBuilder().disposal(null).build()).getSentence())).isNull();
        assertThat((ConvictionTransformer.convictionOf(anEvent().toBuilder().disposal(aDisposal()).build()).getSentence())).isNotNull();

    }

    @Test
    public void enteredSentenceEndDateOverridesExpectedSentenceEndDate() {
        final var expectedEndDate = LocalDate.now().minusDays(1);
        final var enteredEndDate = LocalDate.now().minusDays(2);
        final var disposal = disposalBuilder().enteredSentenceEndDate(enteredEndDate).expectedSentenceEndDate(expectedEndDate).build();
        assertThat(ConvictionTransformer.convictionOf(anEvent().toBuilder().disposal(disposal).build()).getSentence().getExpectedSentenceEndDate()).isEqualTo(enteredEndDate);
    }

    @Test
    public void expectedSentenceEndDateIsMappedIfEnteredSentenceEndDateNotPresent() {
        final var expectedEndDate = LocalDate.now().minusDays(1);
        final var disposal = disposalBuilder().expectedSentenceEndDate(expectedEndDate).build();
        assertThat(ConvictionTransformer.convictionOf(anEvent().toBuilder().disposal(disposal).build()).getSentence().getExpectedSentenceEndDate()).isEqualTo(expectedEndDate);
    }

    @Test
    public void expectedSentenceEndDateMappingCanHandleNulls() {
        assertThat(ConvictionTransformer.convictionOf(anEvent().toBuilder().disposal(aDisposal()).build()).getSentence().getExpectedSentenceEndDate()).isNull();
    }

    @Test
    public void outcomeMappedFromLastCourtAppearance() {
        assertThat(ConvictionTransformer.convictionOf(
                anEvent()
                        .toBuilder()
                        .courtAppearances(ImmutableList.of(
                                aCourtAppearanceWithNoOutcome(LocalDateTime.now()),
                                aCourtAppearance("Final Review", "Y", LocalDateTime.now().minusDays(1)),
                                aCourtAppearance("Adjourned", "X", LocalDateTime.now().minusDays(2))
                                ))
                        .build()).getLatestCourtAppearanceOutcome())
                .isNotNull()
                .hasFieldOrPropertyWithValue("code", "Y")
                .hasFieldOrPropertyWithValue("description", "Final Review");

    }

    @Test
    public void indexMappedFromEventNumberString() {
        assertThat((ConvictionTransformer.convictionOf(anEvent().toBuilder().eventNumber("5").build()).getIndex())).isEqualTo("5");
    }

    @Test
    public void custodyNotSetWhenDisposalNotPresent() {
        assertThat(
                ConvictionTransformer.convictionOf(
                        anEvent()
                                .toBuilder()
                                .disposal(null)
                                .build()
                ).getCustody()
        ).isNull();
    }

    @Test
    public void custodyNotSetWhenCustodyNotPresentInDisposal() {
        assertThat(
                ConvictionTransformer.convictionOf(
                        anEvent()
                                .toBuilder()
                                .disposal(Disposal
                                        .builder()
                                        .custody(null)
                                        .build())
                                .build()
                ).getCustody()
        ).isNull();
    }

    @Test
    public void custodySetWhenCustodyPresentInDisposal() {
        assertThat(
                ConvictionTransformer.convictionOf(
                        anEvent()
                                .toBuilder()
                                .disposal(Disposal
                                        .builder()
                                        .custody(aCustody())
                                        .build())
                                .build()
                ).getCustody()
        ).isNotNull();
    }

    @Test
    public void bookingNumberCopiedFromCustodyPrisonerNumber() {
        assertThat(
                ConvictionTransformer.convictionOf(
                        anEvent()
                                .toBuilder()
                                .disposal(Disposal
                                        .builder()
                                        .custody(
                                                aCustody()
                                                        .toBuilder()
                                                        .prisonerNumber("V74111")
                                                        .build()
                                        )
                                        .build())
                                .build()
                ).getCustody().getBookingNumber()
        ).isEqualTo("V74111");
    }

    @Test
    public void institutionCopiedFromCustodyWhenPresent() {
        assertThat(
                ConvictionTransformer.convictionOf(
                        anEvent()
                                .toBuilder()
                                .disposal(Disposal
                                        .builder()
                                        .custody(
                                                aCustody()
                                                        .toBuilder()
                                                        .prisonerNumber("V74111")
                                                        .institution(anInstitution())
                                                        .build()
                                        )
                                        .build())
                                .build()
                ).getCustody().getInstitution()
        ).isNotNull();
    }

    @Test
    public void institutionNotCopiedFromCustodyWhenNotPresent() {

        assertThat(
                ConvictionTransformer.convictionOf(
                        anEvent()
                                .toBuilder()
                                .disposal(Disposal
                                        .builder()
                                        .custody(
                                                aCustody()
                                                        .toBuilder()
                                                        .prisonerNumber("V74111")
                                                        .institution(null)
                                                        .build()
                                        )
                                        .build())
                                .build()
                ).getCustody().getInstitution()
        ).isNull();
    }



    @Test
    public void unpaidWorkMappedWherePresent() {

        Event event = EntityHelper.anEvent().toBuilder()
                .disposal(Disposal.builder()
                        .unpaidWorkDetails(UpwDetails.builder()
                                .upwLengthMinutes(120L)
                                .status(StandardReference.builder()
                                    .standardReferenceListId(3503L)
                                    .codeValue("IGNORED")
                                    .codeDescription("Being worked")
                                    .build())
                                .appointments(Arrays.asList(
                                        UpwAppointment.builder()
                                                .attended("Y")
                                                .complied("Y")
                                                .minutesCredited(60L)
                                        .build(),
                                        UpwAppointment.builder()
                                                .attended("Y")
                                                .complied("Y")
                                                .minutesCredited(10L)
                                        .build(),
                                        UpwAppointment.builder()
                                                .attended("Y")
                                                .complied("Y")
                                                .minutesCredited(20L)
                                        .build(),
                                        UpwAppointment.builder()
                                                .attended("N")
                                                .complied("Y")
                                                .build(),
                                        UpwAppointment.builder()
                                                .attended("N")
                                                .complied("N")
                                                .build(),
                                        UpwAppointment.builder()
                                                .attended(null)
                                                .complied(null)
                                                .build()
                                        ))
                                .build())
                        .build())
                .build();
        UnpaidWork unpaidWork = ConvictionTransformer.convictionOf(event)
                .getSentence()
                .getUnpaidWork();

        assertThat(unpaidWork).isNotNull();
        assertThat(unpaidWork.getMinutesOrdered()).isEqualTo(120L);
        assertThat(unpaidWork.getMinutesCompleted()).isEqualTo(90L);
        assertThat(unpaidWork.getAppointments().getTotal()).isEqualTo(6);
        assertThat(unpaidWork.getAppointments().getAcceptableAbsences()).isEqualTo(1);
        assertThat(unpaidWork.getAppointments().getUnacceptableAbsences()).isEqualTo(1);
        assertThat(unpaidWork.getAppointments().getNoOutcomeRecorded()).isEqualTo(1);
        assertThat(unpaidWork.getStatus()).isEqualTo("Being worked");

    }

    @Test
    public void unpaidWorkIsNullWhereNonePresent() {
        Event event = EntityHelper.anEvent().toBuilder()
                .disposal(Disposal.builder()
                        .unpaidWorkDetails(null)
                        .build())
                .build();
        Conviction conviction = ConvictionTransformer.convictionOf(event);
        UnpaidWork unpaidWork = conviction.getSentence().getUnpaidWork();
        assertThat(unpaidWork).isNull();
    }

    @Test
    public void sentenceStartDateCopiedWhenPresent() {
        Event event = EntityHelper.anEvent().toBuilder()
                .disposal(Disposal.builder()
                        .startDate(LocalDate.of(2020, 2, 22))
                        .build())
                .build();
        final var conviction = ConvictionTransformer.convictionOf(event);
        assertThat(conviction.getSentence().getStartDate()).isEqualTo(LocalDate.of(2020, 2, 22));
    }

    @Test
    public void sentenceTerminationDetailsCopiedWhenPresent() {

        final StandardReference standardReference = StandardReference.builder()
                                                    .standardReferenceListId(3758L)
                                                    .codeValue("DT02")
                                                    .codeDescription("Auto Terminated")
                                                    .build();

        Event event = EntityHelper.anEvent().toBuilder()
            .disposal(Disposal.builder()
                .terminationDate(LocalDate.of(2020, 2, 22))
                .terminationReason(standardReference)
                .build())
            .build();
        final var conviction = ConvictionTransformer.convictionOf(event);
        assertThat(conviction.getSentence().getTerminationDate()).isEqualTo(LocalDate.of(2020, 2, 22));
        assertThat(conviction.getSentence().getTerminationReason()).isEqualTo("Auto Terminated");
    }

    @Nested
    class CustodyRelatedKeyDatesOf {
        @Test
        void willSetNothingIfNoneExist() {
            final var keyDates = ConvictionTransformer.custodyOf(aCustody().toBuilder().keyDates(List.of()).build())
                    .getKeyDates();

            assertThat(keyDates.getConditionalReleaseDate()).isNull();
            assertThat(keyDates.getExpectedPrisonOffenderManagerHandoverDate()).isNull();
            assertThat(keyDates.getExpectedPrisonOffenderManagerHandoverStartDate()).isNull();
            assertThat(keyDates.getExpectedReleaseDate()).isNull();
            assertThat(keyDates.getHdcEligibilityDate()).isNull();
            assertThat(keyDates.getLicenceExpiryDate()).isNull();
            assertThat(keyDates.getParoleEligibilityDate()).isNull();
            assertThat(keyDates.getPostSentenceSupervisionEndDate()).isNull();
            assertThat(keyDates.getSentenceExpiryDate()).isNull();
        }

        @Test
        void willSetNothingIfNoneOfTheOnesWeAreInterestedInExist() {
            final var keyDates = ConvictionTransformer.custodyOf(aCustody().toBuilder()
                    .keyDates(List.of(aKeyDate("XX", "Whatever", LocalDate
                            .now()))).build())
                    .getKeyDates();

            assertThat(keyDates.getConditionalReleaseDate()).isNull();
            assertThat(keyDates.getExpectedPrisonOffenderManagerHandoverDate()).isNull();
            assertThat(keyDates.getExpectedPrisonOffenderManagerHandoverStartDate()).isNull();
            assertThat(keyDates.getExpectedReleaseDate()).isNull();
            assertThat(keyDates.getHdcEligibilityDate()).isNull();
            assertThat(keyDates.getLicenceExpiryDate()).isNull();
            assertThat(keyDates.getParoleEligibilityDate()).isNull();
            assertThat(keyDates.getPostSentenceSupervisionEndDate()).isNull();
            assertThat(keyDates.getSentenceExpiryDate()).isNull();
        }

        @Test
        void willSetAllIfAllAreSet() {
            final var keyDates = ConvictionTransformer.custodyOf(aCustody().toBuilder()
                    .keyDates(List.of(
                            aKeyDate("LED", "LicenceExpiryDate", LocalDate.of(2030, 1, 1)),
                            aKeyDate("POM2", "ExpectedPrisonOffenderManagerHandoverDate", LocalDate.of(2030, 1, 2)),
                            aKeyDate("POM1", "ExpectedPrisonOffenderManagerHandoverStartDate", LocalDate.of(2030, 1, 3)),
                            aKeyDate("ACR", "ConditionalReleaseDate", LocalDate.of(2030, 1, 4)),
                            aKeyDate("EXP", "ExpectedReleaseDate", LocalDate.of(2030, 1, 5)),
                            aKeyDate("HDE", "HdcEligibilityDate", LocalDate.of(2030, 1, 6)),
                            aKeyDate("PSSED", "PostSentenceSupervisionEndDate", LocalDate.of(2030, 1, 7)),
                            aKeyDate("PED", "ParoleEligibilityDate", LocalDate.of(2030, 1, 8)),
                            aKeyDate("SED", "SentenceExpiryDate", LocalDate.of(2030, 1, 9)),
                            aKeyDate("XX", "Whatever", LocalDate.now())
                    )).build())
                    .getKeyDates();

            assertThat(keyDates.getLicenceExpiryDate()).isEqualTo(LocalDate.of(2030, 1, 1));
            assertThat(keyDates.getExpectedPrisonOffenderManagerHandoverDate()).isEqualTo(LocalDate.of(2030, 1, 2));
            assertThat(keyDates.getExpectedPrisonOffenderManagerHandoverStartDate()).isEqualTo(LocalDate.of(2030, 1, 3));
            assertThat(keyDates.getConditionalReleaseDate()).isEqualTo(LocalDate.of(2030, 1, 4));
            assertThat(keyDates.getExpectedReleaseDate()).isEqualTo(LocalDate.of(2030, 1, 5));
            assertThat(keyDates.getHdcEligibilityDate()).isEqualTo(LocalDate.of(2030, 1, 6));
            assertThat(keyDates.getPostSentenceSupervisionEndDate()).isEqualTo(LocalDate.of(2030, 1, 7));
            assertThat(keyDates.getParoleEligibilityDate()).isEqualTo(LocalDate.of(2030, 1, 8));
            assertThat(keyDates.getSentenceExpiryDate()).isEqualTo(LocalDate.of(2030, 1, 9));
        }
    }

    private CourtAppearance aCourtAppearance(String outcomeDescription, String outcomeCode, LocalDateTime appearanceDate) {
        return CourtAppearance
                .builder()
                .appearanceDate(appearanceDate)
                .outcome(StandardReference
                        .builder()
                        .codeValue(outcomeCode)
                        .codeDescription(outcomeDescription)
                        .build())
                .build();
    }

    private CourtAppearance aCourtAppearanceWithNoOutcome(LocalDateTime appearanceDate) {
        return CourtAppearance
                .builder()
                .appearanceDate(appearanceDate)
                .outcome(null)
                .build();
    }

    private AdditionalOffence anAdditionalOffence() {
        return AdditionalOffence
                .builder()
                .offence(anOffence())
                .build();
    }

    private MainOffence aMainOffence() {
        return MainOffence
                .builder()
                .offence(anOffence())
                .build();
    }

    private Offence anOffence() {
        return Offence
                .builder()
                .ogrsOffenceCategory(StandardReference.builder().build())
                .build();
    }

    private uk.gov.justice.digital.delius.data.api.Offence anApiMainOffence() {
        return uk.gov.justice.digital.delius.data.api.Offence
                .builder()
                .mainOffence(true)
                .build();
    }

    private Event anEvent() {
        return Event
                .builder()
                .additionalOffences(ImmutableList.of())
                .courtAppearances(ImmutableList.of())
                .build();
    }

    private Disposal aDisposal() {
        return disposalBuilder()
                .build();
    }

    private Disposal.DisposalBuilder disposalBuilder() {
        return Disposal.builder()
                .disposalId(1L)
                .event(anEvent())
                .offenderId(1L)
                .softDeleted(0L);
    }

    private CourtCase aCourtCase() {
        return CourtCase
                .builder()
                .offences(ImmutableList.of(anApiMainOffence()))
                .orderManager(uk.gov.justice.digital.delius.data.api.OrderManager.builder().build())
                .build();
    }

    private Custody aCustody() {
        return Custody.builder().build();
    }

    private RInstitution anInstitution() {
        return RInstitution.builder().build();
    }

}
