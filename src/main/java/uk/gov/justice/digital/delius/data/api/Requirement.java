package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Optional;

@Data
@Builder
public class Requirement {
    @ApiModelProperty(required = true)
    private Long requirementId;
    private Optional<String> requirementNotes;
    private LocalDate commencementDate;
    private LocalDate startDate;
    private LocalDate terminationDate;
    private LocalDate expectedStartDate;
    private LocalDate expectedEndDate;
    private boolean active;
    private Optional<KeyValue> requirementTypeSubCategory;
    private Optional<KeyValue> requirementTypeMainCategory;
    private Optional<KeyValue> adRequirementTypeMainCategory;
    private Optional<KeyValue> adRequirementTypeSubCategory;

}
