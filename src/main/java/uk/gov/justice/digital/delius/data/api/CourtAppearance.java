package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CourtAppearance {
    private Long courtAppearanceId;
    private LocalDate appearanceDate;
    private Court court;
}
