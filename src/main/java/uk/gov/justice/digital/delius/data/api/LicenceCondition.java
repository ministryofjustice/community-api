package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LicenceCondition {
    private String licenceConditionNotes;
    private LocalDate startDate;
    private LocalDate commencementDate;
    private String commencementNotes;
    private LocalDate terminationDate;
    private String terminationNotes;
    private LocalDateTime createdDateTime;
    private Boolean active;
    private KeyValue licenceConditionTypeMainCat;

}
