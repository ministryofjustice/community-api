package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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
    private OffsetDateTime lastUpdatedDateTime;
}
