package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ConvictionRequirements {
    @ApiModelProperty(value = "List of requirements associated with this conviction")
    private List<Requirement> requirements;
}
