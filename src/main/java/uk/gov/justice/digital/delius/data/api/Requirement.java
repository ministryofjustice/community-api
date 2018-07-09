package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class Requirement {
    @ApiModelProperty(required = true)
    private Long requirementId;
    private String requirementNotes;
    private LocalDate commencementDate;
    private LocalDate startDate;
    private LocalDate terminationDate;
    private LocalDate expectedStartDate;
    private LocalDate expectedEndDate;
    private boolean active;
    private KeyValue requirementTypeSubCategory;
    private KeyValue requirementTypeMainCategory;
    private KeyValue adRequirementTypeMainCategory;
    private KeyValue adRequirementTypeSubCategory;

}
