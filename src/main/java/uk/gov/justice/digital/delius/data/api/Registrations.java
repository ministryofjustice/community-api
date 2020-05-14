package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel(description = "Registration Wrapper")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Registrations {
    @ApiModelProperty(value = "List of registrations")
    private List<Registration> registrations;
}
