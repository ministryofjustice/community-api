package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableContactOutcomeTypes {
    @ApiModelProperty(name = "Indicates if an outcome is required if the contact date is in the past", required = true)
    private RequiredOptional outcomeRequired;

    @ApiModelProperty(name = "Outcomes available for this contact type", required = true)
    private List<ContactOutcomeTypeDetail> outcomeTypes;
}
