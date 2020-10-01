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
public class CommunityOrPrisonOffenderManager {
    @ApiModelProperty(value = "Staff code", example = "CHSE755")
    private String staffCode;
    @ApiModelProperty(value = "Staff id", example = "123455")
    private Long staffId;
    @ApiModelProperty(value = "True if this offender manager is the current responsible officer", example = "true")
    private Boolean isResponsibleOfficer;
    @ApiModelProperty(value = "True if this offender manager is the prison OM else False", example = "true")
    private Boolean isPrisonOffenderManager;
    @ApiModelProperty(value = "True if no real offender manager has been allocated and this is just a placeholder", example = "true")
    private Boolean isUnallocated;
    @ApiModelProperty(value = "staff name details")
    private Human staff;
    @ApiModelProperty(value = "Team details for this offender manager")
    private Team team;
    @ApiModelProperty(value = "Probation area / prison institution for this OM")
    private ProbationArea probationArea;
    @ApiModelProperty(value = "Date since the offender manager was assigned", example = "2019-12-04")
    private LocalDate fromDate;
}
