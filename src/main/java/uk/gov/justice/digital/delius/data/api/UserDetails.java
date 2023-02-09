package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "User Details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetails {
    @Schema(description = "User ID of the user", example = "12345", required = true)
    private Long userId;
    @Schema(description = "First name of the user", example = "John", required = true)
    private String firstName;
    @Schema(description = "Surname of the user", example = "Smith", required = true)
    private String surname;
    @Schema(description = "Email address of the user", example = "test@digital.justice.gov.uk")
    private String email;
    @Schema(description = "Account is enabled if true", example = "false", required = true)
    private boolean enabled;
    @Schema(description = "Roles For this User")
    private List<UserRole> roles;
    @Schema(description = "The username of the user", example = "test.user")
    private String username;
}
