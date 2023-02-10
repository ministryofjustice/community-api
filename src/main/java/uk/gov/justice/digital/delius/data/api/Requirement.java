package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class Requirement {
    @Schema(description = "Unique identifier for the requirement", required = true)
    private Long requirementId;
    @Schema(description = "Notes added by probation relating to the requirement")
    private String requirementNotes;
    private LocalDate commencementDate;
    private LocalDate startDate;
    private LocalDate terminationDate;
    private LocalDate expectedStartDate;
    private LocalDate expectedEndDate;
    private LocalDateTime createdDatetime;
    @Schema(description = "Is the requirement currently active")
    private Boolean active;
    private KeyValue requirementTypeSubCategory;
    private KeyValue requirementTypeMainCategory;
    private KeyValue adRequirementTypeMainCategory;
    private KeyValue adRequirementTypeSubCategory;
    private KeyValue terminationReason;
    @Schema(description = "The number of temporal units to complete the requirement (see lengthUnit field for unit)")
    private Long length;
    @Schema(description = "The temporal unit corresponding to the length field")
    private String lengthUnit;
    @Schema(description = "Is the main category restrictive")
    private Boolean restrictive;
    private Boolean softDeleted;

    @Schema(description = "Total RAR days completed")
    private Long rarCount;
}
