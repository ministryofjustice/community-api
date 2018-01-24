package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Optional;

@Data
@Builder
public class Requirement {
    private Long requirementId;
    private Optional<String> requirementNotes;
    private Optional<LocalDate> commencementDate;
    private Optional<LocalDate> startDate;
    private Optional<LocalDate> terminationDate;
    private Optional<LocalDate> expectedStartDate;
    private Optional<LocalDate> expectedEndDate;
    private boolean active;
    private Optional<KeyValue> requiremntTypeSubCategory;
    private Optional<KeyValue> adRequirementTypeSubCategory;
    private Optional<KeyValue> requirementTypeMainCategory;

}
