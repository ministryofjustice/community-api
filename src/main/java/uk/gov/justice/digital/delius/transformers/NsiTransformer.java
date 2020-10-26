package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.NsiManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.Nsi;
import uk.gov.justice.digital.delius.jpa.standard.entity.NsiStatus;
import uk.gov.justice.digital.delius.jpa.standard.entity.NsiType;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class NsiTransformer {

    public static final String NSI_LENGTH_UNIT = "Months";
    public static final boolean INCLUDE_PROBATION_AREA_TEAMS = false;

    public static uk.gov.justice.digital.delius.data.api.Nsi nsiOf(Nsi nsi) {
        return Optional.ofNullable(nsi).map(n ->
            uk.gov.justice.digital.delius.data.api.Nsi.builder()
                .nsiId(n.getNsiId())
                .requirement(RequirementTransformer.requirementOf(n.getRqmnt()))
                .nsiType(nsiTypeOf(n.getNsiType()))
                .nsiSubType(nsiSubtypeOf(n.getNsiSubType()))
                .nsiStatus(nsiStatusOf(n.getNsiStatus()))
                .actualStartDate(n.getActualStartDate())
                .expectedStartDate(n.getExpectedStartDate())
                .referralDate(n.getReferralDate())
                .statusDateTime(n.getNsiStatusDateTime())
                .length(n.getLength())
                .lengthUnit(NSI_LENGTH_UNIT)
                .nsiManagers(nsiManagersOf(n.getNsiManagers()))
                .build()).orElse(null);
    }

    private static List<NsiManager> nsiManagersOf(List<uk.gov.justice.digital.delius.jpa.standard.entity.NsiManager> nsiManagers) {
        return nsiManagers.stream()
                .map(nsiManager -> NsiManager.builder()
                        .startDate(nsiManager.getStartDate())
                        .endDate(nsiManager.getEndDate())
                        .probationArea(ProbationAreaTransformer.probationAreaOf(nsiManager.getProbationArea(), INCLUDE_PROBATION_AREA_TEAMS))
                        .team(TeamTransformer.teamOf(nsiManager.getTeam()))
                        .staff(StaffTransformer.staffDetailsOf(nsiManager.getStaff()))
                        .build())
                .collect(Collectors.toList());
    }

    private static KeyValue nsiStatusOf(final NsiStatus nsiStatus) {
        return Optional.ofNullable(nsiStatus).map(nsis ->
            KeyValue.builder().code(nsis.getCode())
                .description(nsis.getDescription())
                .build()).orElse(null);
    }

    private static KeyValue nsiSubtypeOf(final StandardReference nsiSubType) {
        return Optional.ofNullable(nsiSubType).map(nsist ->
            KeyValue.builder()
                .code(nsist.getCodeValue())
                .description(nsist.getCodeDescription())
                .build()).orElse(null);
    }

    private static KeyValue nsiTypeOf(final NsiType nsiType) {
        return Optional.ofNullable(nsiType).map(nsit -> KeyValue.builder()
            .code(nsit.getCode())
            .description(nsit.getDescription())
            .build()).orElse(null);
    }

}
