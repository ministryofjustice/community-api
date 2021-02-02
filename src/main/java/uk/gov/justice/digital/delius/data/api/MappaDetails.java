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
    @ApiModelProperty(value = "MAPPA Level (0=unknown)", example = "1", allowableValues = "0,1,2,3", notes = "If the level is unknown due to an unrecognised Delius MAPPA level code (e.g. one not used anymore) the original Delius level description will still be returned")
    private Integer level;
    @ApiModelProperty(value = "MAPPA Level Description", example = "MAPPA Level 1")
    private String levelDescription;
    @ApiModelProperty(value = "MAPPA Category (0 = unknown)", example = "3", allowableValues = "0,1,2,3", notes = "If the category is unknown due to an unrecognised Delius MAPPA category code (e.g. one not used anymore) the original Delius category  description will still be returned")
    private Integer category;
    @ApiModelProperty(value = "MAPPA Category Description", example = "MAPPA Cat 1")
    private String categoryDescription;
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
