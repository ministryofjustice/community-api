package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.OrderManager;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OrderManagerTransformer {

    public static OrderManager orderManagerOf(uk.gov.justice.digital.delius.jpa.standard.entity.OrderManager entity){
        return OrderManager.builder()
                .name(Stream.of(Optional.ofNullable(entity.getStaff().getForename()),
                (Optional.ofNullable(entity.getStaff().getForname2())),
                (Optional.ofNullable(entity.getStaff().getSurname())))
                    .filter(Optional::isPresent).map(Optional::get)
                    .collect(Collectors.joining(" ")))
                .staffCode(Optional.ofNullable(entity.getStaff().getOfficerCode()).orElse(""))
                .dateStartOfAllocation(entity.getTeam().getStartDate())
                .dateEndOfAllocation(entity.getTeam().getEndDate())
                .officerId(entity.getOrderManagerId())
                .probationAreaId(entity.getProbationArea().getProbationAreaId())
                .teamId(entity.getTeam().getTeamId()).build();
    }

}
