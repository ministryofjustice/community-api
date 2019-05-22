package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;


@Data
@Builder
public class ManagedOffender {
    @ApiModelProperty(required = true)
    private Long offenderId;
    @ApiModelProperty(required = true)
    private String nomsNumber;
    @ApiModelProperty(required = true)
    private String crnNumber;
    @ApiModelProperty(required = true)
    private String surname;
    @ApiModelProperty(required = true)
    private boolean responsibleOfficer;
    @ApiModelProperty(required = true)
    private boolean offenderManager;
    @ApiModelProperty(required = true)
    private boolean prisonOffenderManager;
    @ApiModelProperty(required = true)
    private boolean current;
    @ApiModelProperty(required = true)
    private LocalDate startDate;
    @ApiModelProperty(required = true)
    private LocalDate endDate;
}
