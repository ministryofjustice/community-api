package uk.gov.justice.digital.delius.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sentence {
    private String description;
    private Long originalLength;
    private String originalLengthUnits;
    private Long secondLength;
    private String secondLengthUnits;
    private Long defaultLength;
    private Long effectiveLength;
    private Long lengthInDays;
    private UnpaidWork unpaidWork;
}
