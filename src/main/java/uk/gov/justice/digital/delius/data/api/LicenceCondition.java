package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Data
@Builder
public class LicenceCondition {
    private Optional<String> licenceConditionNotes;
    private LocalDate startDate;
    private LocalDate commencementDate;
    private Optional<String> commencementNotes;
    private LocalDate terminationDate;
    private Optional<String> terminationNotes;
    private LocalDateTime createdDateTime;
    private boolean active;
    private Optional<KeyValue> licenceConditionTypeMainCat;

}
