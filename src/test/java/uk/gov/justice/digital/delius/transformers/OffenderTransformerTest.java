package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalIdentifier;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disability;
import uk.gov.justice.digital.delius.jpa.standard.entity.LocalDeliveryUnit;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderAlias;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.PrisonOffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProviderTeam;
import uk.gov.justice.digital.delius.jpa.standard.entity.Provision;
import uk.gov.justice.digital.delius.jpa.standard.entity.ResponsibleOfficer;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;
import uk.gov.justice.digital.delius.util.EntityHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.util.OffenderHelper.anOffender;

public class OffenderTransformerTest {

    @Test
    public void offenderManagerAllocationReasonMappedFromAllocationReasonInOffenderTransfer() {
        assertThat(OffenderTransformer.offenderManagersOf(ImmutableList.of(aOffenderManager())).get(0).getAllocationReason())
                .isNotNull()
                .hasFieldOrPropertyWithValue("code", "1984")
                .hasFieldOrPropertyWithValue("description", "Reallocation - Inactive Offender");

    }

    @Test
    public void offenderManagerAllocationReasonNullWhenOffenderTransferAbsent() {
        assertThat(OffenderTransformer.offenderManagersOf(
                ImmutableList.of(
                        aOffenderManager()
                                .toBuilder()
                                .allocationReason(null)
                                .build()))
                .get(0).getAllocationReason())
                .isNull();

    }

    @Test
    public void disabilitiesCopiedWithLatestFirst() {
        assertThat(OffenderTransformer.fullOffenderOf(anOffender()
                .toBuilder()
                .disabilities(ImmutableList.of(
                        Disability
                                .builder()
                                .softDeleted(0L)
                                .disabilityId(1L)
                                .startDate(LocalDate.now().minus(5, ChronoUnit.DAYS))
                                .disabilityType(StandardReference
                                        .builder()
                                        .codeValue("A")
                                        .codeDescription("Mental health")
                                        .build())
                                .provisions(ImmutableList.of(Provision
                                    .builder()
                                    .softDeleted(0L)
                                    .provisionID(1L)
                                    .notes("This is an adjustment")
                                    .startDate(LocalDate.now().minus(5, ChronoUnit.DAYS))
                                    .provisionType(StandardReference
                                        .builder()
                                        .codeValue("O")
                                        .codeDescription("Other")
                                        .build())
                                    .build()))
                                .build(),
                        Disability
                                .builder()
                                .softDeleted(0L)
                                .disabilityId(2L)
                                .startDate(LocalDate.now().minus(2, ChronoUnit.DAYS))
                                .disabilityType(StandardReference
                                        .builder()
                                        .codeValue("B")
                                        .codeDescription("Physical health")
                                        .build())
                                .build(),
                        Disability
                                .builder()
                                .softDeleted(0L)
                                .disabilityId(3L)
                                .startDate(LocalDate.now().minus(3, ChronoUnit.DAYS))
                                .disabilityType(StandardReference
                                        .builder()
                                        .codeValue("C")
                                        .codeDescription("No disability")
                                        .build())
                                .build()
                ))
                .build(), null)
                .getOffenderProfile()
                .getDisabilities())
                .extracting("disabilityId")
                .containsExactly(2L, 3L, 1L);

    }

    @Test
    public void deletedDisabilitiesNotCopied() {
        assertThat(OffenderTransformer.fullOffenderOf(anOffender()
                .toBuilder()
                .disabilities(ImmutableList.of(
                        uk.gov.justice.digital.delius.jpa.standard.entity.Disability
                                .builder()
                                .softDeleted(0L)
                                .disabilityId(1L)
                                .startDate(LocalDate.now().minus(5, ChronoUnit.DAYS))
                                .disabilityType(StandardReference
                                        .builder()
                                        .codeValue("A")
                                        .codeDescription("Mental health")
                                        .build())
                                .build(),
                        uk.gov.justice.digital.delius.jpa.standard.entity.Disability
                                .builder()
                                .softDeleted(1L)
                                .disabilityId(2L)
                                .startDate(LocalDate.now().minus(2, ChronoUnit.DAYS))
                                .disabilityType(StandardReference
                                        .builder()
                                        .codeValue("B")
                                        .codeDescription("Physical health")
                                        .build())
                                .build()
                ))
                .build(), null)
                .getOffenderProfile()
                .getDisabilities())
                .extracting("disabilityId")
                .containsExactly(1L);

    }


    @Test
    public void disabilitiesCopiedWithProvisions() {
        var disabilities = ImmutableList.of(Disability
            .builder()
            .softDeleted(0L)
            .disabilityId(1L)
            .startDate(LocalDate.now().minus(5, ChronoUnit.DAYS))
            .disabilityType(StandardReference
                .builder()
                .codeValue("A")
                .codeDescription("Mental health")
                .build())
            .provisions(ImmutableList.of(Provision
                .builder()
                .softDeleted(0L)
                .provisionID(1L)
                .notes("This is an adjustment")
                .startDate(LocalDate.of(2021, 6, 1))
                .provisionType(StandardReference
                    .builder()
                    .codeValue("O")
                    .codeDescription("Other")
                    .build())
                .build()))
            .build(),
            Disability
                .builder()
                .softDeleted(0L)
                .disabilityId(2L)
                .startDate(LocalDate.now().minus(2, ChronoUnit.DAYS))
                .disabilityType(StandardReference
                    .builder()
                    .codeValue("B")
                    .codeDescription("Physical health")
                    .build())
                .build());

        assertThat(OffenderTransformer.fullOffenderOf(anOffender()
            .toBuilder()
            .disabilities(disabilities)
            .build(), null)
            .getOffenderProfile()
            .getDisabilities())
            .extracting("provisions")
            .containsExactly(ImmutableList.of(), ImmutableList.of(uk.gov.justice.digital.delius.data.api.Provision.builder()
                .notes("This is an adjustment")
                .provisionId(1L)
                .startDate(LocalDate.of(2021, 6, 1))
                .provisionType(KeyValue.builder().code("O").description("Other").build())
                .build()));

    }

    @Test
    public void currentlyManagedOffenders() {

        // Get currently managed offenders for this officer
        assertThat(OffenderTransformer.managedOffenderOf(anOfficerWithOffenderManagers(), true)
                .get(0))
                .hasFieldOrPropertyWithValue("nomsNumber", "A1111")
                .hasFieldOrPropertyWithValue("offenderSurname", "SMITH")
                .hasFieldOrPropertyWithValue("staffCode", "AAAA")
                .hasFieldOrPropertyWithValue("staffIdentifier", 3L);
    }

    @Test
    public void allManagedOffenders() {

        // Get current and past managed offenders for this officer
        assertThat(new ArrayList<>(OffenderTransformer.managedOffenderOf(anOfficerWithOffenderManagers(), false)))
                .hasSize(2);
    }

    @Test
    public void currentResponsibleOfficer() {

        // Get the current responsible officers for an offender
        assertThat(OffenderTransformer.responsibleOfficersOf(anOffenderWithManagers(), true)
                .stream()
                .map(uk.gov.justice.digital.delius.data.api.ResponsibleOfficer::getOffenderManagerId)
                .collect(Collectors.toList()))
                .hasSize(1)
                .containsOnly(2L);
    }

    @Test
    public void allResponsibleOfficers() {

        // Get the current and previous reponsible officers assignments for this offender
        assertThat(OffenderTransformer.responsibleOfficersOf(anOffenderWithManagers(), false)
                .stream()
                .map(uk.gov.justice.digital.delius.data.api.ResponsibleOfficer::getOffenderManagerId)
                .collect(Collectors.toList()))
                .hasSize(2)
                .containsAll(Arrays.asList(1L, 2L));
    }

    @Test
    public void ProbationAreaNPSPrivateSectorTransformedToTrue() {
        assertThat(OffenderTransformer.offenderManagersOf(ImmutableList.of(
                aOffenderManager()
                        .toBuilder()
                        .probationArea(npsProbationArea())
                        .build()))
                .get(0).getProbationArea().getNps()).isTrue();
    }

    @Test
    public void ProbationAreaNPSPrivateSectorTransformedToFalse() {
        assertThat(OffenderTransformer.offenderManagersOf(ImmutableList.of(
                aOffenderManager()
                        .toBuilder()
                        .probationArea(crcProbationArea())
                        .build()))
                .get(0).getProbationArea().getNps()).isFalse();
    }

    @Test
    public void currentTierDescriptionCopiedWhenNotNull() {
        assertThat(OffenderTransformer.fullOffenderOf(anOffender().toBuilder().currentTier(
                null).build(), null).getCurrentTier())
                .isNull();

        assertThat(OffenderTransformer.fullOffenderOf(anOffender().toBuilder().currentTier(
                StandardReference
                        .builder()
                        .codeDescription("D2")
                        .build()).build(), null).getCurrentTier())
                .isEqualTo("D2");
    }

    private List<OffenderManager> aListOfOffenderManagers() {

        // List of offender managers managing the same offender with one current and one historical
        return ImmutableList.of(
                aOffenderManager()
                        .toBuilder()
                        .offenderManagerId(1L)
                        .offenderId(1L)
                        .allocationDate(LocalDate.parse("2018-01-01"))
                        .endDate(LocalDate.parse("2019-01-01"))
                        .activeFlag(0L)
                        .probationArea(npsProbationArea())
                        .team(aTeam())
                        .providerTeam(aProviderTeam())
                        .staff(anOfficerWithoutOffenderManagers())
                        .managedOffender(anOffenderWithoutManagers())
                        .build(),

                aOffenderManager()
                        .toBuilder()
                        .offenderManagerId(2L)
                        .offenderId(1L)
                        .allocationDate(LocalDate.parse("2019-01-01"))
                        .activeFlag(1L)
                        .probationArea(crcProbationArea())
                        .team(aTeam())
                        .providerTeam(aProviderTeam())
                        .staff(anOfficerWithoutOffenderManagers())
                        .responsibleOfficers(List.of(aResponsibleOfficer()))
                        .managedOffender(anOffenderWithoutManagers())
                        .build()
        );
    }

    private List<PrisonOffenderManager> aListOfPrisonOffenderManagers() {
        return new ArrayList<>();
    }

    private Offender anOffenderWithoutManagers() {

        return Offender.builder()
                .offenderId(1L)
                .surname("SMITH")
                .nomsNumber("A1111")
                .build();
    }

    private Offender anOffenderWithManagers() {

        return Offender.builder()
                .offenderId(1L)
                .surname("SMITH")
                .nomsNumber("A1111")
                .offenderManagers(aListOfOffenderManagers())
                .prisonOffenderManagers(aListOfPrisonOffenderManagers())
                .build();
    }


    private ResponsibleOfficer aResponsibleOfficer() {
        return ResponsibleOfficer
                .builder()
                .startDateTime(LocalDateTime.parse("2018-01-01T00:00:00"))
                .offenderId(1L)
                .offenderManagerId(2L)
                .build();
    }


    private OffenderManager aOffenderManager() {
        return OffenderManager
                .builder()
                .allocationReason(
                        StandardReference
                                .builder()
                                .codeDescription("Reallocation - Inactive Offender")
                                .codeValue("1984")
                                .build()
                )
                .probationArea(ProbationArea.builder().build())
                .build();
    }

    private Staff anOfficerWithOffenderManagers() {
        return Staff
                .builder()
                .staffId(3L)
                .officerCode("AAAA")
                .surname("Officer")
                .forename("First")
                .forname2("First2")
                .offenderManagers(aListOfOffenderManagers())
                .build();
    }

    private Staff anOfficerWithoutOffenderManagers() {
        return Staff.builder()
                .staffId(3L)
                .officerCode("AAAA")
                .surname("Officer")
                .forename("First")
                .forname2("First2")
                .build();
    }

    private Team aTeam() {
        return Team.builder().teamId(5L).code("A1").description("A1 DESC").localDeliveryUnit(aLocalDeliveryUnit()).build();
    }

    private ProbationArea npsProbationArea() {
        return ProbationArea.builder().probationAreaId(6L).code("PA1").description("PA1 DESC").privateSector(0L).build();
    }

    private ProbationArea crcProbationArea() {
        return ProbationArea.builder().probationAreaId(6L).code("PA2").description("PA2 DESC").privateSector(1L).build();
    }

    private LocalDeliveryUnit aLocalDeliveryUnit() {
        return LocalDeliveryUnit.builder().localDeliveryUnitId(6L).code("LDU1").description("LUD1 DESC").build();
    }

    private ProviderTeam aProviderTeam() {
        return ProviderTeam.builder().build();

    }

    @Nested
    class AdditionalIdentifiersOf {
        @Test
        void willCopyPrimaryIdentifiers() {
            final var offender = Offender
                    .builder()
                    .offenderId(99L)
                    .crn("X123456")
                    .nomsNumber("A1234CR")
                    .pncNumber("2004/0712343H")
                    .croNumber("123456/04A")
                    .niNumber("AA112233B")
                    .immigrationNumber("A1234567")
                    .mostRecentPrisonerNumber("G12345")
                    .build();

            final var ids = OffenderTransformer.idsOf(offender);

            assertThat(ids.getNomsNumber()).isEqualTo("A1234CR");
            assertThat(ids.getPncNumber()).isEqualTo("2004/0712343H");
            assertThat(ids.getCroNumber()).isEqualTo("123456/04A");
            assertThat(ids.getNiNumber()).isEqualTo("AA112233B");
            assertThat(ids.getImmigrationNumber()).isEqualTo("A1234567");
            assertThat(ids.getMostRecentPrisonerNumber()).isEqualTo("G12345");
        }

        @Test
        void willCopyAdditionalIdentifiers() {
            final var offender = Offender
                    .builder()
                    .additionalIdentifiers(List.of(
                            AdditionalIdentifier
                                    .builder()
                                    .additionalIdentifierId(999L)
                                    .identifierName(StandardReference
                                            .builder()
                                            .codeDescription("Duplicate NOMS number")
                                            .codeValue("DNOMS")
                                            .build())
                                    .identifier("A1234XX")
                                    .softDeleted(0L)
                                    .build(),
                            AdditionalIdentifier
                                    .builder()
                                    .additionalIdentifierId(998L)
                                    .identifierName(StandardReference
                                            .builder()
                                            .codeDescription("Former NOMS number")
                                            .codeValue("XNOMS")
                                            .build())
                                    .identifier("A1234YY")
                                    .softDeleted(0L)
                                    .build()

                    ))
                    .build();

            final var additionalIdentifiers = OffenderTransformer
                    .additionalIdentifiersOf(offender.getAdditionalIdentifiers());

            assertThat(additionalIdentifiers)
                    .hasSize(2)
                    .containsExactly(uk.gov.justice.digital.delius.data.api.AdditionalIdentifier
                                    .builder()
                                    .additionalIdentifierId(999L)
                                    .value("A1234XX")
                                    .type(KeyValue
                                            .builder()
                                            .code("DNOMS")
                                            .description("Duplicate NOMS number")
                                            .build())
                                    .build(),
                            uk.gov.justice.digital.delius.data.api.AdditionalIdentifier
                                    .builder()
                                    .additionalIdentifierId(998L)
                                    .value("A1234YY")
                                    .type(KeyValue
                                            .builder()
                                            .code("XNOMS")
                                            .description("Former NOMS number")
                                            .build())
                                    .build()
                    );

        }

        @Test
        void willNotCopyDeletedAdditionalIdentifiers() {
            final var offender = Offender
                    .builder()
                    .additionalIdentifiers(List.of(
                            AdditionalIdentifier
                                    .builder()
                                    .additionalIdentifierId(999L)
                                    .identifierName(StandardReference
                                            .builder()
                                            .codeDescription("Duplicate NOMS number")
                                            .codeValue("DNOMS")
                                            .build())
                                    .identifier("A1234XX")
                                    .softDeleted(0L)
                                    .build(),
                            AdditionalIdentifier
                                    .builder()
                                    .additionalIdentifierId(998L)
                                    .identifierName(StandardReference
                                            .builder()
                                            .codeDescription("Former NOMS number")
                                            .codeValue("XNOMS")
                                            .build())
                                    .identifier("A1234YY")
                                    .softDeleted(1L)
                                    .build()

                    ))
                    .build();

            final var additionalIdentifiers = OffenderTransformer
                    .additionalIdentifiersOf(offender.getAdditionalIdentifiers());

            assertThat(additionalIdentifiers).hasSize(1);

        }
    }

    @Test
    void aliasesAreCopied() {
        final var offenderEntity = EntityHelper.anOffender().toBuilder().offenderAliases(List.of(
                OffenderAlias
                        .builder()
                        .aliasID(99L)
                        .dateOfBirth(LocalDate.parse("1965-07-19"))
                        .firstName("John")
                        .surname("Smith")
                        .build(),
                OffenderAlias
                        .builder()
                        .aliasID(100L)
                        .dateOfBirth(LocalDate.parse("1965-07-20"))
                        .firstName("Johnny")
                        .surname("Smyth")
                        .build()

        )).build();

        final var offender = OffenderTransformer.fullOffenderOf(offenderEntity, null);

        assertThat(offender.getOffenderAliases())
                .hasSize(2)
                .containsExactly(
                        uk.gov.justice.digital.delius.data.api.OffenderAlias
                                .builder()
                                .id("99")
                                .dateOfBirth(LocalDate.parse("1965-07-19"))
                                .firstName("John")
                                .surname("Smith")
                                .middleNames(List.of())
                                .build(),
                        uk.gov.justice.digital.delius.data.api.OffenderAlias
                                .builder()
                                .id("100")
                                .dateOfBirth(LocalDate.parse("1965-07-20"))
                                .firstName("Johnny")
                                .surname("Smyth")
                                .middleNames(List.of())
                                .build()
                );
    }

    @Test
    void addressesAreTransformed() {
        final var source = EntityHelper.anOffender();
        final var address = source.getOffenderAddresses().get(0);

        final var observed = OffenderTransformer.fullOffenderOf(source, null).getContactDetails().getAddresses();

        assertThat(observed)
            .hasSize(1)
            .first()
            .hasFieldOrPropertyWithValue("town", address.getTownCity())
            .hasFieldOrPropertyWithValue("type.code", "ATAP01")
            .hasFieldOrPropertyWithValue("type.description", "Approved Premises Type")
            .hasFieldOrPropertyWithValue("typeVerified", true)
            .hasFieldOrPropertyWithValue("latestAssessmentDate", LocalDateTime.of(2010, 6, 11, 12, 0))
            .hasFieldOrPropertyWithValue("from", address.getStartDate())
            .hasFieldOrPropertyWithValue("to", address.getEndDate())
            .hasFieldOrPropertyWithValue("status.code", "M")
            .hasFieldOrPropertyWithValue("status.description", "Main")
            .hasFieldOrPropertyWithValue("noFixedAbode", true)
            .usingRecursiveComparison()
            .ignoringFields("town", "type", "typeVerified", "latestAssessmentDate", "from", "to", "status", "noFixedAbode")
            .isEqualTo(address);
    }

    @Test
    void basicInfoCopied() {
        final var source = EntityHelper.anOffender();

        final var observed = OffenderTransformer.fullOffenderOf(source, null);

        assertThat(observed)
            .hasFieldOrPropertyWithValue("firstName", "Bill")
            .hasFieldOrPropertyWithValue("middleNames", List.of("Arthur", "Steve"))
            .hasFieldOrPropertyWithValue("surname", "Sykes")
            .hasFieldOrPropertyWithValue("preferredName", "Bob")
            .hasFieldOrPropertyWithValue("offenderProfile.genderIdentity", "Prefer to self describe")
            .hasFieldOrPropertyWithValue("offenderProfile.selfDescribedGender", "Jedi");
    }


    @Test
    public void isActiveIsTrueWhenStartDateTodayAndEndDateInFuture() {
        assertThat(OffenderTransformer.fullOffenderOf(anOffender()
                .toBuilder()
                .disabilities(ImmutableList.of(
                    Disability
                        .builder()
                        .softDeleted(0L)
                        .disabilityId(2L)
                        .startDate(LocalDate.now())
                        .finishDate(LocalDate.now().plusDays(1))
                        .disabilityType(StandardReference.builder().build())
                        .build()
                ))
                .build(), null)
            .getOffenderProfile()
            .getDisabilities())
            .extracting("isActive")
            .containsExactly(true);
    }

    @Test
    public void isActiveIsTrueWhenEndDateIsNull() {
        assertThat(OffenderTransformer.fullOffenderOf(anOffender()
                .toBuilder()
                .disabilities(ImmutableList.of(
                    Disability
                        .builder()
                        .softDeleted(0L)
                        .disabilityId(3L)
                        .startDate(LocalDate.now())
                        .disabilityType(StandardReference.builder().build())
                        .build()
                ))
                .build(), null)
            .getOffenderProfile()
            .getDisabilities())
            .extracting("isActive")
            .containsExactly(true);
    }

    @Test
    public void isActiveIsFalseWhenEndDateIsToday() {
        assertThat(OffenderTransformer.fullOffenderOf(anOffender()
                .toBuilder()
                .disabilities(ImmutableList.of(
                    Disability
                        .builder()
                        .softDeleted(0L)
                        .disabilityId(4L)
                        .startDate(LocalDate.now())
                        .finishDate(LocalDate.now())
                        .disabilityType(StandardReference.builder().build())
                        .build()
                ))
                .build(), null)
            .getOffenderProfile()
            .getDisabilities())
            .extracting("isActive")
            .containsExactly(false);
    }

    @Test
    public void isActiveIsFalseWhenStartDateInFuture() {
        assertThat(OffenderTransformer.fullOffenderOf(anOffender()
                .toBuilder()
                .disabilities(ImmutableList.of(
                    Disability
                        .builder()
                        .softDeleted(0L)
                        .disabilityId(1L)
                        .startDate(LocalDate.now().plusDays(1))
                        .disabilityType(StandardReference.builder().build())
                        .build()
                ))
                .build(), null)
            .getOffenderProfile()
            .getDisabilities())
            .extracting("isActive")
            .containsExactly(false);
    }
}
