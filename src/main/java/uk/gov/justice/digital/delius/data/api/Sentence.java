package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
    @ApiModelProperty(value = "Unpaid Work to date associated with this sentence")
    private UnpaidWork unpaidWork;
    @ApiModelProperty(value = "Date sentence started")
    private LocalDate startDate;
    private LocalDate terminationDate;
    private String terminationReason;
}
