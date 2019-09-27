package uk.gov.justice.digital.delius.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
