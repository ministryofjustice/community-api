package uk.gov.justice.digital.delius.data.api;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class StaffDetails {
    @ApiModelProperty(value = "the optional username of this staff member, will be absent if the staff member is not a user of Delius", example = "SheilaHancockNPS")
    private String username;
    @ApiModelProperty(value = "the optional email address of this staff member, will be absent if the staff member is not a user of Delius", example = "sheila.hancock@test.justice.gov.uk")
    private String email;
    @ApiModelProperty(value = "staff code AKA officer code", example = "SH0001")
    private String staffCode;
    @ApiModelProperty(value = "staff identifier", example = "123456")
    private Long staffIdentifier;
    @ApiModelProperty(value = "staff name details")
    private Human staff;
    @ApiModelProperty(value = "all teams related to this staff member")
    private List<Team> teams;
}
