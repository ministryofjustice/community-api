package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "A set of managed entities in Delius")
public class Caseload {
    @ApiModelProperty(value = "Managed offender CRNs")
    private Set<ManagedOffenderCrn> managedOffenders;
    @ApiModelProperty(value = "Managed order/event/conviction identifiers")
    private Set<ManagedEventId> supervisedOrders;
}
