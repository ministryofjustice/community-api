package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel(description = "Offender Identifiers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffenderIdentifiers {
    @ApiModelProperty(value = "unique identifier for this offender", example = "1234567")
    private Long offenderId;
    @ApiModelProperty(value = "Primary identifiers")
    private IDs primaryIdentifiers;
    @ApiModelProperty(value = "Additional identifiers")
    private List<AdditionalIdentifier> additionalIdentifiers;
}
