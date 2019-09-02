package uk.gov.justice.digital.delius.data.api;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderManager {
    private Long probationAreaId;
    private Long teamId;
    private Long officerId;
}
