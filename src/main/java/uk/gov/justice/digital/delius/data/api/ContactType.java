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
public class ContactType {
    @ApiModelProperty(required = true)
    private String code;
    @ApiModelProperty(required = true)
    private String description;
    private String shortDescription;

    @ApiModelProperty(name = "Does this contact type represent an appointment type")
    private Boolean appointment;

    @ApiModelProperty(name = "Does this contact type represent a national standard contact")
    private Boolean nationalStandard;

    @ApiModelProperty(name = "Active categories this contact type belongs belongs to")
    private List<KeyValue> categories;

    @ApiModelProperty(name = "Does this contact type represent a system generated type")
    private Boolean systemGenerated;
}
