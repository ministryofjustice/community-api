package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;


@Data
@Builder
public class ManagedOffender {
    @ApiModelProperty(required = true)
    private String staffCode;
    @ApiModelProperty(required = true)
    private Long offenderId;
    @ApiModelProperty(required = true)
    private String nomsNumber;
    @ApiModelProperty(required = true)
    private String crnNumber;
    @ApiModelProperty(required = true)
    private String offenderSurname;
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
