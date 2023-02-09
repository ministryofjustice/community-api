package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Schema(description = "Risk Resourcing Details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskResourcingDetails {
    @Schema(description = "decision")
    private ResourcingDecision decision;

    @Schema(description = "This is equivalent to indicating if the person is retained by NPS when there was a NPS/CRC split. true = requires enhanced resourcing as if they were allocated to the NPS", example = "true")
    private Boolean enhancedResourcing;
    @Schema(description = "id of the conviction that lead to the decision. Decision are related to the conviction", example = "1219491")
    private Long relatedConvictionId;

    @Schema(description = "Risk Resourcing Details")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourcingDecision {
        @Schema(description = "Date decision was made", example = "2021-04-27")
        private LocalDate date;
        @Schema(description = "The decision code", example = "R")
        private String code;
        @Schema(description = "The decision description", example = "Retained")
        private String description;
    }
}

