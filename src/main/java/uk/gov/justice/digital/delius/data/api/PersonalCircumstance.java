package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PersonalCircumstance {
    private Long personalCircumstanceId;
    private Long offenderId;
    private KeyValue type;
    private KeyValue subType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private KeyValue probationArea;
    private String notes;
    private Boolean evidenced;

}
