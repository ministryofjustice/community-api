package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.jpa.standard.entity.CaseAllocation;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CaseAllocationTransformerTest {

    @Test
    @DisplayName("will copy decision")
    void riskResourcingDetailsOfCopiesDecision() {
        final var caseAllocation = CaseAllocation
            .builder()
            .allocationDecision(StandardReference
                .builder()
                .codeValue("R")
                .codeDescription("Retained")
                .build())
            .allocationDecisionDate(LocalDateTime.parse("2020-07-19T10:00:00"))
            .event(Event.builder().eventId(88L).build())
            .build();

        final var resourcingDetails = CaseAllocationTransformer.riskResourcingDetailsOf(caseAllocation);
        assertThat(resourcingDetails.getDecision().getDate()).isEqualTo(LocalDate.parse("2020-07-19"));
        assertThat(resourcingDetails.getDecision().getDescription()).isEqualTo("Retained");
        assertThat(resourcingDetails.getDecision().getCode()).isEqualTo("R");
        assertThat(resourcingDetails.getRelatedConvictionId()).isEqualTo(88L);
    }

    @Test
    @DisplayName("will indicated if decision is enhanced resourcing when retained")
    void enhancedIsSetWhenRetained() {
        final var caseAllocation = CaseAllocation
            .builder()
            .allocationDecision(StandardReference
                .builder()
                .codeValue("R")
                .codeDescription("Retained")
                .build())
            .allocationDecisionDate(LocalDateTime.parse("2020-07-19T10:00:00"))
            .event(Event.builder().eventId(88L).build())
            .build();

        final var resourcingDetails = CaseAllocationTransformer.riskResourcingDetailsOf(caseAllocation);
        assertThat(resourcingDetails.getEnhancedResourcing()).isTrue();
    }

    @Test
    @DisplayName("allows decision not be present")
    void allowsDecisionNotBePresent() {

        final var caseAllocation = CaseAllocation
            .builder()
            .allocationDecision(null)
            .allocationDecisionDate(null)
            .event(Event.builder().eventId(88L).build())
            .build();

        final var resourcingDetails = CaseAllocationTransformer.riskResourcingDetailsOf(caseAllocation);
        assertThat(resourcingDetails.getEnhancedResourcing()).isNull();
        assertThat(resourcingDetails.getDecision()).isNull();
        assertThat(resourcingDetails.getRelatedConvictionId()).isEqualTo(88L);
    }


    @Test
    @DisplayName("will indicated if decision is not enhanced resourcing when allocated")
    void notEnhancedIsSetWhenAllocated() {
        final var caseAllocation = CaseAllocation
            .builder()
            .allocationDecision(StandardReference
                .builder()
                .codeValue("A")
                .codeDescription("Allocated")
                .build())
            .allocationDecisionDate(LocalDateTime.parse("2020-07-19T10:00:00"))
            .event(Event.builder().eventId(88L).build())
            .build();

        final var resourcingDetails = CaseAllocationTransformer.riskResourcingDetailsOf(caseAllocation);
        assertThat(resourcingDetails.getEnhancedResourcing()).isFalse();
    }
}
