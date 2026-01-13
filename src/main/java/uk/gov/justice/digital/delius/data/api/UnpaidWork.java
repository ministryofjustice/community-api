package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Jacksonized
public class UnpaidWork {
    @Schema(description = "Minutes of unpaid work ordered for this sentence")
    private Long minutesOrdered;
    @Schema(description = "Minutes of unpaid work credited to the service user to date")
    private Long minutesCompleted;
    @Schema(description = "Details of appointment history to date")
    private Appointments appointments;
    @Schema(description = "Status description")
    private String status;
}
