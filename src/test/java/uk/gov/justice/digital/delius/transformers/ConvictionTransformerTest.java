package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.data.api.CourtCase;
import uk.gov.justice.digital.delius.data.api.Institution;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;
import uk.gov.justice.digital.delius.service.LookupSupplier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConvictionTransformerTest {
    @Mock
    private LookupSupplier lookupSupplier;
    @Mock
    private MainOffenceTransformer mainOffenceTransformer;
    @Mock
    private AdditionalOffenceTransformer additionalOffenceTransformer;
    @Mock
    private CourtAppearanceTransformer courtAppearanceTransformer;
    @Mock
    private InstitutionTransformer institutionTransformer;
    private ConvictionTransformer transformer;

    @Before
    public void before() {
        transformer = new ConvictionTransformer(
                mainOffenceTransformer,
                additionalOffenceTransformer,
                courtAppearanceTransformer,
                lookupSupplier,
                institutionTransformer);

        when(lookupSupplier.userSupplier()).thenReturn(() -> User.builder().userId(99L).build());
        when(lookupSupplier.orderAllocationReasonSupplier()).thenReturn(code -> StandardReference.builder().codeValue(code).build());
        when(lookupSupplier.transferReasonSupplier()).thenReturn(code -> TransferReason.builder().code(code).build());
        when(lookupSupplier.probationAreaSupplier()).thenReturn(orderManager -> ProbationArea.builder().probationAreaId(orderManager.getProbationAreaId()).build());
        when(lookupSupplier.teamSupplier()).thenReturn(orderManager -> Team.builder().teamId(orderManager.getTeamId()).build());
        when(lookupSupplier.staffSupplier()).thenReturn(orderManager -> Staff.builder().staffId(orderManager.getOfficerId()).build());

        when(courtAppearanceTransformer.courtAppearanceOf(any(), any(), any())).thenReturn(aCourtAppearanceWithNoOutcome(LocalDateTime.now()));
    }

    @Test
    public void convictionIdMappedFromEventId() {
        assertThat(transformer.convictionOf(
                anEvent()
                    .toBuilder()
                    .eventId(99L)
                    .build()).getConvictionId()
        ).isEqualTo(99L);

    }

    @Test
    public void offencesCollatedFromMainAndAdditionalOffences() {
        transformer = new ConvictionTransformer(
                new MainOffenceTransformer(lookupSupplier),
                new AdditionalOffenceTransformer(lookupSupplier),
                courtAppearanceTransformer,
                lookupSupplier,
                institutionTransformer);

        assertThat(transformer.convictionOf(
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
        assertThat((transformer.convictionOf(anEvent().toBuilder().activeFlag(1L).build()).getActive())).isTrue();
        assertThat((transformer.convictionOf(anEvent().toBuilder().activeFlag(0L).build()).getActive())).isFalse();

    }

    @Test
    public void inBreachMappedForZeroOneInBreach() {
        assertThat((transformer.convictionOf(anEvent().toBuilder().inBreach(1L).build()).getInBreach())).isTrue();
        assertThat((transformer.convictionOf(anEvent().toBuilder().inBreach(0L).build()).getInBreach())).isFalse();

    }

    @Test
    public void sentenceIsMappedWhenEvenHasDisposal() {
        assertThat((transformer.convictionOf(anEvent().toBuilder().disposal(null).build()).getSentence())).isNull();
        assertThat((transformer.convictionOf(anEvent().toBuilder().disposal(aDisposal()).build()).getSentence())).isNotNull();

    }

    @Test
    public void outcomeMappedFromLastCourtAppearance() {
        assertThat(transformer.convictionOf(
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
        assertThat((transformer.convictionOf(anEvent().toBuilder().eventNumber("5").build()).getIndex())).isEqualTo("5");
    }

    @Test
    public void custodyNotSetWhenDisposalNotPresent() {
        assertThat(
                transformer.convictionOf(
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
                transformer.convictionOf(
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
                transformer.convictionOf(
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
                transformer.convictionOf(
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
        when(institutionTransformer.institutionOf(any())).thenReturn(Institution.builder().build());

        assertThat(
                transformer.convictionOf(
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
                transformer.convictionOf(
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
    public void offenderIdCopiedToEvent() {
        assertThat((transformer.eventOf(99L, aCourtCase(), "1").getOffenderId())).isEqualTo(99L);
    }

    @Test
    public void setsAuditFields() {
        when(lookupSupplier.userSupplier()).thenReturn(() -> User.builder().userId(99L).build());
        final Event event = transformer.eventOf(99L, aCourtCase(), "1");

        assertThat(event.getCreatedByUserId()).isEqualTo(99L);
        assertThat(event.getLastUpdatedUserId()).isEqualTo(99L);
        assertThat(event.getCreatedDatetime()).isNotNull();
        assertThat(event.getLastUpdatedDatetime()).isNotNull();
    }


    @Test
    public void setsSensibleDefaults() {
        final Event event = transformer.eventOf(99L, aCourtCase(), "1");

        assertThat(event.getSoftDeleted()).isEqualTo(0L);
        assertThat(event.getPartitionAreaId()).isEqualTo(0L);
        assertThat(event.getRowVersion()).isEqualTo(1L);
        assertThat(event.getActiveFlag()).isEqualTo(1L);
        assertThat(event.getInBreach()).isEqualTo(0L);
        assertThat(event.getPendingTransfer()).isEqualTo(0L);
        assertThat(event.getPostSentenceSupervisionRequirementFlag()).isEqualTo(0L);
    }

    @Test
    public void orderManagerIsCreatedFromTeamAreaStaffLookups() {
        final Event event = transformer.eventOf(
                99L,
                aCourtCase()
                        .toBuilder()
                        .orderManager(
                                uk.gov.justice.digital.delius.data.api.OrderManager
                                        .builder()
                                        .officerId(2L)
                                        .probationAreaId(3L)
                                        .teamId(4L)
                                        .build())
                        .build(),
                "1");

        assertThat(event.getOrderManagers()).hasSize(1);
        assertThat(event.getOrderManagers().get(0).getStaff().getStaffId()).isEqualTo(2L);
        assertThat(event.getOrderManagers().get(0).getProbationArea().getProbationAreaId()).isEqualTo(3L);
        assertThat(event.getOrderManagers().get(0).getTeam().getTeamId()).isEqualTo(4L);
    }

    @Test
    public void orderManagerProviderElementsNeverSet() {
        final Event event = transformer.eventOf(99L, aCourtCase(), "1");

        assertThat(event.getOrderManagers()).hasSize(1);
        assertThat(event.getOrderManagers().get(0).getProviderTeam()).isNull();
        assertThat(event.getOrderManagers().get(0).getProviderEmployee()).isNull();
    }

    @Test
    public void orderManagerTransferReasonIsAlwaysCaseOrder() {
        final Event event = transformer.eventOf(99L, aCourtCase(), "1");

        assertThat(event.getOrderManagers()).hasSize(1);
        assertThat(event.getOrderManagers().get(0).getTransferReason().getCode()).isEqualTo("CASE ORDER");
    }

    @Test
    public void orderManagerIsNotEndDated() {
        final Event event = transformer.eventOf(99L, aCourtCase(), "1");

        assertThat(event.getOrderManagers()).hasSize(1);
        assertThat(event.getOrderManagers().get(0).getEndDate()).isNull();
    }

    @Test
    public void orderManagerAllocationReasonIsAlwaysNexEventCreated() {
        final Event event = transformer.eventOf(99L, aCourtCase(), "1");

        assertThat(event.getOrderManagers()).hasSize(1);
        assertThat(event.getOrderManagers().get(0).getAllocationReason().getCodeValue()).isEqualTo("IN1");

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
        return Disposal.builder()
                .disposalId(1L)
                .event(anEvent())
                .offenderId(1L)
                .softDeleted(0L)
                .build();
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
