package uk.gov.justice.digital.delius.transformers;

import org.flywaydb.core.internal.util.StringUtils;
import uk.gov.justice.digital.delius.data.api.OrderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;

import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

public class OrderManagerTransformer {

    public static OrderManager orderManagerOf(uk.gov.justice.digital.delius.jpa.standard.entity.OrderManager entity) {
        return OrderManager.builder()
                .name(ofNullable(entity.getStaff())
                    .map(staff -> Stream.of(staff.getForename(), staff.getForname2(), staff.getSurname())
                        .filter(StringUtils::hasLength)
                        .collect(joining(" ")))
                    .orElse(null))
                .staffCode(ofNullable(entity.getStaff()).map(Staff::getOfficerCode).orElse(null))
                .gradeCode(ofNullable(entity.getStaff()).map(Staff::getGrade).map(StandardReference::getCodeValue).orElse(null))
                .teamId(ofNullable(entity.getTeam()).map(Team::getTeamId).orElse(null))
                .teamCode(ofNullable(entity.getTeam()).map(Team::getCode).orElse(null))
                .dateStartOfAllocation(entity.getAllocationDate())
                .dateEndOfAllocation(entity.getEndDate())
                .officerId(entity.getOrderManagerId())
                .probationAreaId(entity.getProbationArea().getProbationAreaId())
                .probationAreaCode(entity.getProbationArea().getCode())
                .build();
    }
}
