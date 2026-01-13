package uk.gov.justice.digital.delius.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Jacksonized
public class SentenceStatus {
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
