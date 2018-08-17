package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonPropertyOrder({"offenceId", "mainOffence"})
public class Offence {
    private String offenceId;
    private Boolean mainOffence;
    private OffenceDetail detail;
    private LocalDateTime offenceDate;
    private Long offenceCount;
    private Long tics;
    private String verdict;
    private Boolean softDeleted;
    private Long offenderId;
    private LocalDateTime createdDatetime;
    private LocalDateTime lastUpdatedDatetime;
}
