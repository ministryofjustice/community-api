package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ResponsibleOfficer {

    @ApiModelProperty(required = true)
    private Long responsibleOfficerId;
    @ApiModelProperty(required = true)
    private Long offenderManagerId;
    @ApiModelProperty(required = true)
    private String staffCode;
    private String forenames;
    @ApiModelProperty(required = true)
    private String surname;
    private String providerTeamCode;
    private String providerTeamDesc;
    private String LduCode;
    private String LduDesc;
    private String probationAreaCode;
    private String probationAreaDesc;
    @ApiModelProperty(required = true)
    private boolean active;
    @ApiModelProperty(required = true)
    private LocalDate startDate;
    @ApiModelProperty(required = true)
    private LocalDate endDate;
    @ApiModelProperty(required = true)
    private boolean isPom;
    @ApiModelProperty(required = true)
    private boolean isOm;
}
