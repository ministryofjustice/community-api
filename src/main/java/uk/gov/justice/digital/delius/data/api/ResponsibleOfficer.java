package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponsibleOfficer {

    @Schema(required=true)
    private String nomsNumber;
    @Schema
    private Long responsibleOfficerId;
    @Schema
    private Long offenderManagerId;
    @Schema
    private Long prisonOffenderManagerId;
    @Schema(required = true)
    private String staffCode;
    @Schema(required = true)
    private String surname;
    private String forenames;
    private String providerTeamCode;
    private String providerTeamDescription;
    private String lduCode;
    private String lduDescription;
    private String probationAreaCode;
    private String probationAreaDescription;
    @Schema(required = true)
    private boolean isCurrentRo;
    @Schema(required = true)
    private boolean isCurrentOm;
    @Schema(required = true)
    private boolean isCurrentPom;
    @Schema(required = true)
    private LocalDate omStartDate;
    @Schema(required = true)
    private LocalDate omEndDate;
}
