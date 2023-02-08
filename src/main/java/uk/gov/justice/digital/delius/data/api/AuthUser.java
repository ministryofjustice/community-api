package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@ApiModel(description = "Authentication Details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthUser {
    @NotBlank
    @ApiModelProperty(value = "LDAP username", example = "TEST_USER_NPS", required = true, position = 1)
    private String username;

    @NotBlank
    @ApiModelProperty(value = "LDAP password", example = "password123456", required = true, position = 2)
    private String password;
}
