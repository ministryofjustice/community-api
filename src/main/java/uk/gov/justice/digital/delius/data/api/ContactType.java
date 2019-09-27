package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactType {
    @ApiModelProperty(required = true)
    private String code;
    @ApiModelProperty(required = true)
    private String description;
    private String shortDescription;
}
