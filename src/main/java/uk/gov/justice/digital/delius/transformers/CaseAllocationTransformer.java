package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.RiskResourcingDetails;
import uk.gov.justice.digital.delius.data.api.RiskResourcingDetails.ResourcingDecision;
import uk.gov.justice.digital.delius.jpa.standard.entity.CaseAllocation;

import java.util.Optional;

public class CaseAllocationTransformer {
    private static final String ASSESSED_ENHANCED = "R";
    private static final String NOT_ASSESSED = "N";

    public static RiskResourcingDetails riskResourcingDetailsOf(final CaseAllocation caseAllocation) {
        final var resourcingDecision = Optional
            .ofNullable(caseAllocation.getAllocationDecision())
            .map(decision -> RiskResourcingDetails.ResourcingDecision.builder()
                .code(decision.getCodeValue())
                .description(decision.getCodeDescription())
                .date(caseAllocation.getAllocationDecisionDate().toLocalDate())
                .build())
            .orElse(null);
        return RiskResourcingDetails
            .builder()
            .relatedConvictionId(caseAllocation.getEvent().getEventId())
            .decision(resourcingDecision)
            .enhancedResourcing(isEnhanced(resourcingDecision))
            .build();
    }

    private static Boolean isEnhanced(final ResourcingDecision resourcingDecision) {
        return Optional
            .ofNullable(resourcingDecision)
            .filter(decision -> !decision.getCode().equals(NOT_ASSESSED))
            .map(decision -> decision.getCode().equals(ASSESSED_ENHANCED))
            .orElse(null);
    }
}
