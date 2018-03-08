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
    private Optional<LocalDate> startDate;
    private Optional<LocalDate> commencementDate;
    private Optional<String> commencementNotes;
    private Optional<LocalDate> terminationDate;
    private Optional<String> terminationNotes;
    private Optional<LocalDateTime> createdDateTime;
    private boolean active;
    private Optional<KeyValue> licenceConditionTypeMainCat;

}
