package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;

import java.sql.Timestamp;
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
    private OffenderTransformer offenderTransformer = new OffenderTransformer(new ContactTransformer());

    @Test
    public void offenderManagerAllocationReasonMappedFromAllocationReasonInOffenderTransfer() {
        assertThat(offenderTransformer.offenderManagersOf(ImmutableList.of(aOffenderManager())).get(0).getAllocationReason())
                .isNotNull()
                .hasFieldOrPropertyWithValue("code", "1984")
                .hasFieldOrPropertyWithValue("description", "Reallocation - Inactive Offender");

    }

    @Test
    public void offenderManagerAllocationReasonNullWhenOffenderTransferAbsent() {
        assertThat(offenderTransformer.offenderManagersOf(
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
        assertThat(offenderTransformer.fullOffenderOf(anOffender()
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
                                .softDeleted(0L)
                                .disabilityId(2L)
                                .startDate(LocalDate.now().minus(2, ChronoUnit.DAYS))
                                .disabilityType(StandardReference
                                        .builder()
                                        .codeValue("B")
                                        .codeDescription("Physical health")
                                        .build())
                                .build(),
                        uk.gov.justice.digital.delius.jpa.standard.entity.Disability
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
                .build())
                .getOffenderProfile()
                .getDisabilities())
                .extracting("disabilityId")
                .containsExactly(2L, 3L, 1L);

    }

    @Test
    public void deletedDisabilitiesNotCopied() {
        assertThat(offenderTransformer.fullOffenderOf(anOffender()
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
                .build())
                .getOffenderProfile()
                .getDisabilities())
                .extracting("disabilityId")
                .containsExactly(1L);

    }

    @Test
    public void currentlyManagedOffenders() {

        // Get currently managed offenders for this officer
        assertThat(offenderTransformer.managedOffenderOf(anOfficerWithOffenderManagers(), true)
                .get(0))
                .hasFieldOrPropertyWithValue("nomsNumber", "A1111")
                .hasFieldOrPropertyWithValue("offenderSurname", "SMITH")
                .hasFieldOrPropertyWithValue("staffCode", "AAAA");
    }

    @Test
    public void allManagedOffenders() {

        // Get current and past managed offenders for this officer
        assertThat(offenderTransformer.managedOffenderOf(anOfficerWithOffenderManagers(), false)
                .stream()
                .collect(Collectors.toList()))
                .hasSize(2);
    }

    @Test
    public void currentResponsibleOfficer() {

        // Get the current responsible officers for an offender
        assertThat(offenderTransformer.responsibleOfficersOf(anOffenderWithManagers(), true)
                .stream()
                .map(ro -> ro.getOffenderManagerId())
                .collect(Collectors.toList()))
                .hasSize(1)
                .containsOnly(2L);
    }

    @Test
    public void allResponsibleOfficers() {

        // Get the current and previous reponsible officers assignments for this offender
        assertThat(offenderTransformer.responsibleOfficersOf(anOffenderWithManagers(), false)
                .stream()
                .map(ro -> ro.getOffenderManagerId())
                .collect(Collectors.toList()))
                .hasSize(2)
                .containsAll(Arrays.asList(1L, 2L));
    }

    @Test
    public void ProbationAreaNPSPrivateSectorTransformedToTrue() {
        assertThat(offenderTransformer.offenderManagersOf(ImmutableList.of(
                aOffenderManager()
                        .toBuilder()
                        .probationArea(npsProbationArea())
                        .build()))
                .get(0).getProbationArea().getNps()).isTrue();
    }

    @Test
    public void ProbationAreaNPSPrivateSectorTransformedToFalse() {
        assertThat(offenderTransformer.offenderManagersOf(ImmutableList.of(
                aOffenderManager()
                        .toBuilder()
                        .probationArea(crcProbationArea())
                        .build()))
                .get(0).getProbationArea().getNps()).isFalse();
    }


    private List<OffenderManager> aListOfOffenderManagers() {

        // List of offender managers managing the same offender with one current and one historical
        List<OffenderManager> offenderManagers = ImmutableList.of(

                aOffenderManager()
                        .toBuilder()
                        .offenderManagerId(1L)
                        .offenderId(1L)
                        .allocationDate(Timestamp.valueOf(LocalDateTime.parse("2018-01-01T00:00:00")))
                        .endDate(Timestamp.valueOf(LocalDateTime.parse("2019-01-01T00:00:00")))
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
                        .allocationDate(Timestamp.valueOf(LocalDateTime.parse("2019-01-01T00:00:00")))
                        .activeFlag(1L)
                        .probationArea(crcProbationArea())
                        .team(aTeam())
                        .providerTeam(aProviderTeam())
                        .staff(anOfficerWithoutOffenderManagers())
                        .responsibleOfficer(aResponsibleOfficer())
                        .managedOffender(anOffenderWithoutManagers())
                        .build()
        );

        return offenderManagers;
    }

    private List<PrisonOffenderManager> aListOfPrisonOffenderManagers() {

        List<PrisonOffenderManager> prisonOffenderManagers = new ArrayList<PrisonOffenderManager>();
        return prisonOffenderManagers;
    }

    private Offender anOffenderWithoutManagers() {

        Offender offender = Offender.builder()
                .offenderId(1L)
                .surname("SMITH")
                .nomsNumber("A1111")
                .build();

        return offender;
    }

    private Offender anOffenderWithManagers() {

        Offender offender = Offender.builder()
                .offenderId(1L)
                .surname("SMITH")
                .nomsNumber("A1111")
                .offenderManagers(aListOfOffenderManagers())
                .prisonOffenderManagers(aListOfPrisonOffenderManagers())
                .build();

        return offender;
    }


    private ResponsibleOfficer aResponsibleOfficer() {
        return ResponsibleOfficer
                .builder()
                .startDate(LocalDate.parse("2018-01-01"))
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

}
