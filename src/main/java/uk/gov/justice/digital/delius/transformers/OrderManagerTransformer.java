package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.OrderManager;

public class OrderManagerTransformer {

    public static OrderManager orderManagerOf(uk.gov.justice.digital.delius.jpa.standard.entity.OrderManager entity){
        return OrderManager.builder().
            name(entity.getStaff().getForename().concat(" "+entity.getStaff().getSurname())).
            staffCode(entity.getStaff().getStaffId().toString()).
            dateStartOfAllocation(entity.getTeam().getStartDate()).
            dateEndOfAllocation(entity.getTeam().getEndDate()).
            officerId(entity.getOrderManagerId()).
            probationAreaId(entity.getProbationArea().getProbationAreaId()).
            teamId(entity.getTeam().getTeamId()).build();
    }
}
