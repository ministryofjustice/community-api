package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CourtAppearanceBasic {
    @ApiModelProperty(name = "Unique id of this court appearance", example = "2500000001")
    private Long courtAppearanceId;

    @ApiModelProperty(name = "Date of this court appearance", example = "2019-09-04T00:00:00")
    private LocalDateTime appearanceDate;

    @ApiModelProperty(name = "Appearance court code", example = "SHEFMC")
    private String courtCode;

    @ApiModelProperty(name = "Appearance court name", example = "Sheffield Magistrates Court")
    private String courtName;

    @ApiModelProperty(name = "Type of this court appearance")
    private KeyValue appearanceType;

    @ApiModelProperty(name = "Offender CRN")
    private String crn;
}
