package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
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
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.AuthPassword;
import uk.gov.justice.digital.delius.data.api.AuthUser;
import uk.gov.justice.digital.delius.service.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Api(tags = "Authentication and users")
@Validated
public class AuthenticationController {

    private final UserService userService;

    @ApiOperation(
        value = "Authenticate a username and password against Delius Identity (LDAP)",
        authorizations = {@Authorization("ROLE_COMMUNITY_AUTH_INT")})
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY_AUTH_INT"),
        })
    @PreAuthorize("hasRole('ROLE_COMMUNITY_AUTH_INT')")
    @PostMapping("/authenticate")
    public void authenticate(@NotNull @Valid @ApiParam(value = "Authentication Details", required = true) @RequestBody final AuthUser authUser) {
        if (!userService.authenticateUser(authUser.getUsername(), authUser.getPassword())) {
            throw new UnauthorisedException(String.format("User with username %s", authUser.getUsername()));
        }
    }

    @ApiOperation(
        value = "Change password a users (LDAP) account",
        authorizations = {@Authorization("ROLE_COMMUNITY_AUTH_INT")})
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "Password Changed"),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY_AUTH_INT"),
            @ApiResponse(code = 404, message = "Not Found", response = ErrorResponse.class)
        })
    @PreAuthorize("hasRole('ROLE_COMMUNITY_AUTH_INT')")
    @PostMapping(value = "/users/{username}/password")
    public void changePassword(@ApiParam(name = "username", value = "LDAP username", example = "TESTUSERNPS", required = true) @NotNull final @PathVariable("username") String username,
                               @NotNull @Valid @ApiParam(value = "Password Credentials", required = true) @RequestBody final AuthPassword authPassword) {
        if (!userService.changePassword(username, authPassword.getPassword())) {
            throw new NotFoundException(String.format("User with username %s", username));
        }
    }
}
