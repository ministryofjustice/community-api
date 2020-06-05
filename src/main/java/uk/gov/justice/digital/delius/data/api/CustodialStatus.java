package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class CustodialStatus {
    private Long sentenceId;
    private KeyValue custodialType;
    private KeyValue sentence;
    private KeyValue mainOffence;
    private LocalDate sentenceDate;
    private LocalDate actualReleaseDate;
    private LocalDate licenceExpiryDate;
    private LocalDate pssEndDate;
    private Long length;
    private String lengthUnit;
}
