package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Appointments {
    @Schema(description = "Total number of appointments to date")
    private Long total;
    @Schema(description = "Number of appointments recorded to date as attended")
    private Long attended;
    @Schema(description = "Number of appointments recorded to date as not attended and compliant - i.e. with an acceptable reason for absence")
    private Long acceptableAbsences;
    @Schema(description = "Number of appointments recorded to date as not attended and uncompliant - i.e. without an acceptable reason for absence")
    private Long unacceptableAbsences;
    @Schema(description = "Number of appointments to date where no attendance or compliance information has been recorded")
    private Long noOutcomeRecorded;
}
