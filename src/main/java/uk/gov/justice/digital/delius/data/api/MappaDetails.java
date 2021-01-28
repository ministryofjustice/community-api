package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@ApiModel(description = "MAPPA Details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MappaDetails {
    @ApiModelProperty(value = "MAPPA Level", example = "1")
    private Integer level;
    @ApiModelProperty(value = "MAPPA Category", example = "3")
    private Integer category;
    @ApiModelProperty(value = "Start date", example = "2021-01-27")
    private LocalDate startDate;
    @ApiModelProperty(value = "Next review date", example = "2021-04-27")
    private LocalDate reviewDate;
    @ApiModelProperty(value = "Team code", example = "N07SP1")
    private String teamCode;
    @ApiModelProperty(value = "Officer code", example = "N07A011")
    private String officerCode;
    @ApiModelProperty(value = "Probation area code", example = "N07")
    private String probationAreaCode;
}
