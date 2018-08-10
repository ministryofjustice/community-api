package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Offence {
    private Long id;
    private Boolean mainOffence;
    private OffenceDetail detail;
    private LocalDateTime offenceDate;
    private Long offenceCount;
    private Long eventId;
    private Long tics;
    private String verdict;
    private Boolean softDeleted;
    private Long offenderId;
    private LocalDateTime createdDatetime;
    private LocalDateTime lastUpdatedDatetime;
}
