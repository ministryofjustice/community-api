package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContactType {
    @ApiModelProperty(required = true)
    private String code;
    @ApiModelProperty(required = true)
    private String description;
    private String shortDescription;
}
