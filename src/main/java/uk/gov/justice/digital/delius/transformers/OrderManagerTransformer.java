package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.OrderManager;

import java.util.Optional;

public class OrderManagerTransformer {

    public static OrderManager orderManagerOf(uk.gov.justice.digital.delius.jpa.standard.entity.OrderManager entity){
        return OrderManager.builder().
            name(Optional.ofNullable(entity.getStaff().getForename()).orElse("").
                concat(" "+Optional.ofNullable(entity.getStaff().getSurname()).orElse(""))).
            staffCode(Optional.ofNullable(entity.getStaff().getStaffId()).map(x->x.toString()).orElse("")).
            dateStartOfAllocation(entity.getTeam().getStartDate()).
            dateEndOfAllocation(entity.getTeam().getEndDate()).
            officerId(entity.getOrderManagerId()).
            probationAreaId(entity.getProbationArea().getProbationAreaId()).
            teamId(entity.getTeam().getTeamId()).build();
    }
}
