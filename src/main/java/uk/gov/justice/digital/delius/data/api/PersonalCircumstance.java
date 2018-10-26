package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PersonalCircumstance {
    private Long personalCircumstanceId;
    private Long offenderId;
    private KeyValue personalCircumstanceType;
    private KeyValue personalCircumstanceSubType;
    private LocalDate startDate;
    private LocalDate endDate;
    private KeyValue probationArea;
    private String notes;
    private Boolean evidenced;

}
