package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Authentication Details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthUser {
    @NotBlank
    @Schema(description = "LDAP username", example = "TEST_USER_NPS", required = true)
    private String username;

    @NotBlank
    @Schema(description = "LDAP password", example = "password123456", required = true)
    private String password;
}
