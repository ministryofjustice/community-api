package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConvictionRequirements {
    @ApiModelProperty(value = "List of requirements associated with this conviction")
    private List<Requirement> requirements;
}
