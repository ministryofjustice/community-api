package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.controller.UnauthorisedException;
import uk.gov.justice.digital.delius.data.api.AuthPassword;
import uk.gov.justice.digital.delius.data.api.AuthUser;
import uk.gov.justice.digital.delius.service.UserService;

@RestController
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Tag(name = "Authentication and users")
@Validated
public class AuthenticationController {

    private final UserService userService;

    @Operation(description = "Change password a users (LDAP) account. Requiers ROLE_COMMUNITY_AUTH_INT")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Password Changed"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Requires role ROLE_COMMUNITY_AUTH_INT"),
            @ApiResponse(responseCode = "404", description = "Not Found")
        })
    @PreAuthorize("hasRole('ROLE_COMMUNITY_AUTH_INT')")
    @PostMapping(value = "/users/{username}/password")
    public void changePassword(@Parameter(name = "username", description = "LDAP username", example = "TESTUSERNPS", required = true) @NotNull final @PathVariable("username") String username,
                               @NotNull @Valid @Parameter(description = "Password Credentials", required = true) @RequestBody final AuthPassword authPassword) {
        if (!userService.changePassword(username, authPassword.getPassword())) {
            throw new NotFoundException(String.format("User with username %s", username));
        }
    }
}
