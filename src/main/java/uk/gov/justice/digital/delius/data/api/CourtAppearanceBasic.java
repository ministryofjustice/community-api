package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CourtAppearanceBasic {
    @Schema(name = "Unique id of this court appearance", example = "2500000001")
    private Long courtAppearanceId;

    @Schema(name = "Date of this court appearance", example = "2019-09-04T00:00:00")
    private LocalDateTime appearanceDate;

    @Schema(name = "Appearance court code", example = "SHEFMC")
    private String courtCode;

    @Schema(name = "Appearance court name", example = "Sheffield Magistrates Court")
    private String courtName;

    @Schema(name = "Type of this court appearance")
    private KeyValue appearanceType;

    @Schema(name = "Offender CRN")
    private String crn;
}
