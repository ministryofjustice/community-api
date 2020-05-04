package uk.gov.justice.digital.delius.transformers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.NsiManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.Nsi;
import uk.gov.justice.digital.delius.jpa.standard.entity.NsiStatus;
import uk.gov.justice.digital.delius.jpa.standard.entity.NsiType;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class NsiTransformer {

    public static final String NSI_LENGTH_UNIT = "Months";
    private final RequirementTransformer requirementTransformer;
    private final ProbationAreaTransformer probationAreaTransformer;
    private final CourtTransformer courtTransformer;

    public NsiTransformer(@Autowired final RequirementTransformer requirementTransformer,
                          @Autowired ProbationAreaTransformer probationAreaTransformer,
                          @Autowired CourtTransformer courtTransformer) {
        this.requirementTransformer = requirementTransformer;
        this.probationAreaTransformer = probationAreaTransformer;
        this.courtTransformer = courtTransformer;
    }

    public NsiTransformer() {
        this.requirementTransformer = new RequirementTransformer();
        this.probationAreaTransformer = new ProbationAreaTransformer();
        this.courtTransformer = new CourtTransformer();
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
                .length(n.getLength())
                .lengthUnit(NSI_LENGTH_UNIT)
                .nsiManagers(nsiManagersOf(n.getNsiManagers()))
                .court(courtTransformer.courtOf(nsi.getEvent().getCourt()))
                .build()).orElse(null);
    }

    private List<NsiManager> nsiManagersOf(List<uk.gov.justice.digital.delius.jpa.standard.entity.NsiManager> nsiManagers) {
        return nsiManagers.stream()
                .map(nsiManager -> NsiManager.builder()
                        .startDate(nsiManager.getStartDate())
                        .endDate(nsiManager.getEndDate())
                        .probationArea(probationAreaTransformer.probationAreaOf(nsiManager.getProbationArea()))
                        .build())
                .collect(Collectors.toList());
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
