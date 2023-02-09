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
public class CommunityOrPrisonOffenderManager {
    @Schema(description = "Staff code", example = "CHSE755")
    private String staffCode;
    @Schema(description = "Staff id", example = "123455")
    private Long staffId;
    @Schema(description = "True if this offender manager is the current responsible officer", example = "true")
    private Boolean isResponsibleOfficer;
    @Schema(description = "True if this offender manager is the prison OM else False", example = "true")
    private Boolean isPrisonOffenderManager;
    @Schema(description = "True if no real offender manager has been allocated and this is just a placeholder", example = "true")
    private Boolean isUnallocated;
    @Schema(description = "staff name and contact details")
    private ContactableHuman staff;
    @Schema(description = "Team details for this offender manager")
    private Team team;
    @Schema(description = "Probation area / prison institution for this OM")
    private ProbationArea probationArea;
    @Schema(description = "Date since the offender manager was assigned", example = "2019-12-04")
    private LocalDate fromDate;
    @Schema(description = "Grade details for this offender manager")
    private KeyValue grade;
}
