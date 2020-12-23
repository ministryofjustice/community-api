package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReferralSent {
    @ApiModelProperty(required = false)
    private Long referralSentId;

    @ApiModelProperty(required = true)
    private String contactType;

    @ApiModelProperty(required = true)
    private String probationArea;

    @ApiModelProperty(required = true)
    private String providerTeam;

    @ApiModelProperty(required = true)
    private String probationOfficer;

    @ApiModelProperty(required = true)
    private String employeeId;

    @ApiModelProperty(required = true)
    private String context;
}
