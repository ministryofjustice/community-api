package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Sentence {
    private String description;
    private Long originalLength;
    private String originalLengthUnits;
    private Long secondLength;
    private String secondLengthUnits;
    private Long defaultLength;
    private Long effectiveLength;
    private Long lengthInDays;
}
