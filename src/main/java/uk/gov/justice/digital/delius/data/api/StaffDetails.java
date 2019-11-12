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
    @ApiModelProperty(value = "the optional username of this staff member, maybe absent of the staff member is not a user of the system", required = false)
    private String username;
    @ApiModelProperty(value = "the optional not be present", required = false)
    private String email;
    private String staffCode;
    private Human staff;
    private List<Team> teams;
}
