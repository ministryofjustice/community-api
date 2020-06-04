package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.CustodialStatus;
import uk.gov.justice.digital.delius.data.api.CustodialType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;

@Component
public class CustodialStatusTransformer {
    public CustodialStatus custodialStatusOf(Disposal disposal) {
        return CustodialStatus.builder()
                .custodialType(CustodialType.builder()
                        .code(disposal.getCustody().getCustodialStatus().getCodeValue())
                        .build())
        .build();
    }
}
