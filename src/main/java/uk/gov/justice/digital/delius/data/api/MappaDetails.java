package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Schema(description = "MAPPA Details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MappaDetails {
    @Schema(example = "1", allowableValues = "0,1,2,3", description = "MAPPA Level (0=unknown). If the level is unknown due to an unrecognised Delius MAPPA level code (e.g. one not used anymore) the original Delius level description will still be returned")
    private Integer level;
    @Schema(description = "MAPPA Level Description", example = "MAPPA Level 1")
    private String levelDescription;
    @Schema(example = "3", allowableValues = "0,1,2,3", description = "MAPPA Category (0 = unknown). If the category is unknown due to an unrecognised Delius MAPPA category code (e.g. one not used anymore) the original Delius category  description will still be returned")
    private Integer category;
    @Schema(description = "MAPPA Category Description", example = "MAPPA Cat 1")
    private String categoryDescription;
    @Schema(description = "Start date", example = "2021-01-27")
    private LocalDate startDate;
    @Schema(description = "Next review date", example = "2021-04-27")
    private LocalDate reviewDate;
    @Schema(description = "Team")
    private KeyValue team;
    @Schema(description = "Officer")
    private StaffHuman officer;
    @Schema(description = "Probation area")
    private KeyValue probationArea;
    @Schema(description = "Notes")
    private String notes;
}
