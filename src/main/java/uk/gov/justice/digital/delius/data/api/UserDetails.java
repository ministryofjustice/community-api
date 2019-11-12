package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel(description = "User Details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetails {
    @ApiModelProperty(value = "First name of the user", example = "John", required = true, position = 1)
    private String firstName;
    @ApiModelProperty(value = "Surname of the user", example = "Smith", required = true, position = 2)
    private String surname;
    @ApiModelProperty(value = "Email address of the user", example = "test@digital.justice.gov.uk", position = 3)
    private String email;
    @ApiModelProperty(value = "Account is locked if true", example = "false", required = true, position = 4)
    private boolean locked;
    @ApiModelProperty(value = "Roles For this User", position = 5, allowEmptyValue = true)
    private List<UserRole> roles;
}
