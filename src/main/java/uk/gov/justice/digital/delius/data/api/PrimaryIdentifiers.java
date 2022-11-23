package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "Offender primary identifiers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrimaryIdentifiers {
    @ApiModelProperty(value = "unique identifier for this offender", example = "1234567")
    private Long offenderId;
    @ApiModelProperty(value = "case reference number", required = true, example = "12345C")
    private String crn;
}
