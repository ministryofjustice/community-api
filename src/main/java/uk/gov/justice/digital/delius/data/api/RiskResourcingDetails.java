package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@ApiModel(description = "Risk Resourcing Details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskResourcingDetails {
    @ApiModelProperty(value = "decision")
    private ResourcingDecision decision;

    @ApiModelProperty(value = "enhancedResourcing", example = "true", notes = "This is equivalent to indicating if the person is retained by NPS when there was a NPS/CRC split")
    private Boolean enhancedResourcing;
    @ApiModelProperty(value = "id of the conviction that lead to the decision", example = "1219491", notes = "Decision are related to the conviction")
    private Long relatedConvictionId;

    @ApiModel(description = "Risk Resourcing Details")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourcingDecision {
        @ApiModelProperty(value = "Date decision was made", example = "2021-04-27")
        private LocalDate date;
        @ApiModelProperty(value = "The decision code", example = "R")
        private String code;
        @ApiModelProperty(value = "The decision description", example = "Retained")
        private String description;
    }
}

