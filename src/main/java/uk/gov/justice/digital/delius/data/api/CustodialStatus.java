package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class CustodialStatus {
    private Long sentenceId;
    private CustodialType custodialType;
    private String mainOffenceDescription;
    private String status;
    private LocalDate sentenceDate;
    private LocalDate actualReleaseDate;
    private LocalDate licenceExpiryDate;
    private LocalDate pssEndDate;
    private Integer length;
    private String lengthUnit;
}
