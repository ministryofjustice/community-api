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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.UnauthorisedException;
import uk.gov.justice.digital.delius.data.api.AuthUser;
import uk.gov.justice.digital.delius.service.UserService;

@RestController
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Tag(name = "Authentication and users")
@Validated
public class AuthenticationController {

    private final UserService userService;

    @Operation(description = "Authenticate a username and password against Delius Identity (LDAP). Requires ROLE_COMMUNITY_AUTH_INT")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Requires role ROLE_COMMUNITY_AUTH_INT"),
        })
    @PreAuthorize("hasRole('ROLE_COMMUNITY_AUTH_INT')")
    @PostMapping("/authenticate")
    public void authenticate(@NotNull @Valid @Parameter(description = "Authentication Details", required = true) @RequestBody final AuthUser authUser) {
        if (!userService.authenticateUser(authUser.getUsername(), authUser.getPassword())) {
            throw new UnauthorisedException(String.format("User with username %s", authUser.getUsername()));
        }
    }
}
