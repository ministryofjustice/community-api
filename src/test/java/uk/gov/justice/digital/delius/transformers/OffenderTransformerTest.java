package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
                                        .codeDescription("Metal health")
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
                                        .codeDescription("Metal health")
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

}