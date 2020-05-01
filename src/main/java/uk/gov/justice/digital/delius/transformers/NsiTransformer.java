package uk.gov.justice.digital.delius.transformers;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.standard.entity.Nsi;
import uk.gov.justice.digital.delius.jpa.standard.entity.NsiStatus;
import uk.gov.justice.digital.delius.jpa.standard.entity.NsiType;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

@Component
public class NsiTransformer {

    private final RequirementTransformer requirementTransformer;

    public NsiTransformer (@Autowired final RequirementTransformer requirementTransformer) {
        this.requirementTransformer = requirementTransformer;
    }

    public uk.gov.justice.digital.delius.data.api.Nsi nsiOf(Nsi nsi) {
        return Optional.ofNullable(nsi).map(n ->
            uk.gov.justice.digital.delius.data.api.Nsi.builder()
                .nsiId(n.getNsiId())
                .requirement(requirementTransformer.requirementOf(n.getRqmnt()))
                .nsiType(nsiTypeOf(n.getNsiType()))
                .nsiSubType(nsiSubtypeOf(n.getNsiSubType()))
                .nsiStatus(nsiStatusOf(n.getNsiStatus()))
                .actualStartDate(n.getActualStartDate())
                .expectedStartDate(n.getExpectedStartDate())
                .referralDate(n.getReferralDate())
                .build()).orElse(null);
    }

    private KeyValue nsiStatusOf(final NsiStatus nsiStatus) {
        return Optional.ofNullable(nsiStatus).map(nsis ->
            KeyValue.builder().code(nsis.getCode())
                .description(nsis.getDescription())
                .build()).orElse(null);
    }

    private KeyValue nsiSubtypeOf(final StandardReference nsiSubType) {
        return Optional.ofNullable(nsiSubType).map(nsist ->
            KeyValue.builder()
                .code(nsist.getCodeValue())
                .description(nsist.getCodeDescription())
                .build()).orElse(null);
    }

    private KeyValue nsiTypeOf(final NsiType nsiType) {
        return Optional.ofNullable(nsiType).map(nsit -> KeyValue.builder()
            .code(nsit.getCode())
            .description(nsit.getDescription())
            .build()).orElse(null);
    }

}
