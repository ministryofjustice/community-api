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
public class Team {
    @ApiModelProperty(value = "Team code", example = "C01T04")
    private String code;
    @ApiModelProperty(value = "Team description", example = "OMU A")
    private String description;
    @ApiModelProperty(value = "Team telephone, often not populated", required = false, example = "OMU A")
    private String telephone;
    @ApiModelProperty(value = "Team email address", required = false, example = "first.last@digital.justice.gov.uk")
    private String emailAddress;
    @ApiModelProperty(value = "Local Delivery Unit - provides a geographic grouping of teams")
    private KeyValue localDeliveryUnit;
    @ApiModelProperty(value = "Team Type - provides a logical, not necessarily geographic, grouping of teams")
    private KeyValue teamType;
    @ApiModelProperty(value = "Team's district")
    private KeyValue district;
    @ApiModelProperty(value = "Team's borough")
    private KeyValue borough;
}
