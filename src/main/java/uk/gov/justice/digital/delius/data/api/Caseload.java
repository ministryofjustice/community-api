package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A set of managed entities in Delius")
public class Caseload {
    @Schema(description = "Managed offender CRNs")
    private Set<ManagedOffenderCrn> managedOffenders;
    @Schema(description = "Managed order/event/conviction identifiers")
    private Set<ManagedEventId> supervisedOrders;
}
