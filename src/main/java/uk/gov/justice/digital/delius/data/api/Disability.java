package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Disability {
    private Long disabilityId;
    private KeyValue disabilityType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;
    private List<Provision> provisions;

    @ApiModelProperty(name = "Date time when disability was last updated", example = "2020-09-20T11:00:00+01:00")
    private LocalDateTime lastUpdatedDateTime;
    @ApiModelProperty(value = "The active status of this disability, if the start date is before or on today and the end date is after today or null", example = "true")
    private Boolean isActive;
}
