package uk.gov.justice.digital.delius.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CourtAppearanceMinimal {
    private Long offenderId;
    private Long courtAppearanceId;
    private LocalDateTime appearanceDate;
    private String courtCode;
    private String courtName;
    private KeyValue appearanceType;
}
