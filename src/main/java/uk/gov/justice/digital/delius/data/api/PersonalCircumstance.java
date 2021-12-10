package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalCircumstance {
    @ApiModelProperty(value = "Unique id of this personal circumstance", example = "2500064995")
    private Long personalCircumstanceId;
    @ApiModelProperty(value = "Unique id of this offender", example = "2500343964")
    private Long offenderId;
    @ApiModelProperty(value = "The type of personal circumstance")
    private KeyValue personalCircumstanceType;
    @ApiModelProperty(value = "The type of sub personal circumstance")
    private KeyValue personalCircumstanceSubType;
    @JsonFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "When the offender started this circumstance", example = "2019-09-11")
    private LocalDate startDate;
    @JsonFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "When the offender ended this circumstance", example = "2019-09-11")
    private LocalDate endDate;
    @ApiModelProperty(value = "The probation area that added this circumstance")
    private KeyValue probationArea;
    @ApiModelProperty(value = "Additional notes")
    private String notes;
    @ApiModelProperty(value = "true if evidence was supplied for this circumstance", example = "true")
    private Boolean evidenced;
    @ApiModelProperty(name = "Date and time that this personal circumstance was created", example = "2021-06-11T13:00:00")
    private LocalDateTime createdDatetime;
    @ApiModelProperty(name = "Date and time that this personal circumstance was last updated", example = "2021-06-11T14:00:00")
    private LocalDateTime lastUpdatedDatetime;
    @ApiModelProperty(value = "The active status of this personal circumstance, if the start date is before or on today and the end date is after today or null", example = "true")
    private Boolean isActive;
}
