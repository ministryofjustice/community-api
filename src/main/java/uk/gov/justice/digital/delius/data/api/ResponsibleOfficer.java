package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(required=true)
    private String nomsNumber;
    @ApiModelProperty
    private Long responsibleOfficerId;
    @ApiModelProperty
    private Long offenderManagerId;
    @ApiModelProperty
    private Long prisonOffenderManagerId;
    @ApiModelProperty(required = true)
    private String staffCode;
    @ApiModelProperty(required = true)
    private String surname;
    private String forenames;
    private String providerTeamCode;
    private String providerTeamDescription;
    private String lduCode;
    private String lduDescription;
    private String probationAreaCode;
    private String probationAreaDescription;
    @ApiModelProperty(required = true)
    private boolean isCurrentRo;
    @ApiModelProperty(required = true)
    private boolean isCurrentOm;
    @ApiModelProperty(required = true)
    private boolean isCurrentPom;
    @ApiModelProperty(required = true)
    private LocalDate omStartDate;
    @ApiModelProperty(required = true)
    private LocalDate omEndDate;
}
