package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.CustodialStatus;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;

@Component
public class CustodialStatusTransformer {
    public CustodialStatus custodialStatusOf(Disposal disposal) {
        return CustodialStatus.builder()
                .sentenceId(disposal.getDisposalId())
                .custodialType(KeyValue.builder()
                        .code(disposal.getCustody().getCustodialStatus().getCodeValue())
                        .description(disposal.getCustody().getCustodialStatus().getCodeDescription())
                        .build())
                .sentence(KeyValue.builder()
                        .description(disposal.getDisposalType().getDescription())
                        .build())
                .mainOffence(KeyValue.builder()
                        .description(disposal.getEvent().getMainOffence().getOffence().getDescription()).build())
                .sentenceDate(disposal.getStartDate())
                .actualReleaseDate(disposal.getCustody().getReleases().stream().findFirst().get().getActualReleaseDate().toLocalDate())
                .pssEndDate(disposal.getCustody().getPssEndDate())
                .licenceExpiryDate(disposal.getCustody().getPssStartDate())
                .length(disposal.getLength())
                .lengthUnit("Months")
        .build();
    }
}
