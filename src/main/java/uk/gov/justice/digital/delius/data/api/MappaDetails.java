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
    @ApiModelProperty(value = "MAPPA Level", example = "1", allowableValues = "0,1,2,3")
    private Integer level;
    @ApiModelProperty(value = "MAPPA Category", example = "3", allowableValues = "0,1,2,3")
    private Integer category;
    @ApiModelProperty(value = "Start date", example = "2021-01-27")
    private LocalDate startDate;
    @ApiModelProperty(value = "Next review date", example = "2021-04-27")
    private LocalDate reviewDate;
    @ApiModelProperty(value = "Team")
    private KeyValue team;
    @ApiModelProperty(value = "Officer")
    private StaffHuman officer;
    @ApiModelProperty(value = "Probation area")
    private KeyValue probationArea;
    @ApiModelProperty(value = "Notes")
    private String notes;
}
