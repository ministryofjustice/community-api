package uk.gov.justice.digital.delius.jpa.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffenderDelta {
    private Long offenderId;
    private LocalDateTime dateChanged;
    private String action;
}
