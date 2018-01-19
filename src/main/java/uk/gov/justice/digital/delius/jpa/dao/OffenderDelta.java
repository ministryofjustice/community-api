package uk.gov.justice.digital.delius.jpa.dao;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OffenderDelta {
    private Long offenderId;
    private LocalDateTime dateChanged;
}
