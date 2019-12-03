package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.controller.UnauthorisedException;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.AuthPassword;
import uk.gov.justice.digital.delius.data.api.AuthUser;
import uk.gov.justice.digital.delius.data.api.UserDetails;
import uk.gov.justice.digital.delius.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "Authentication", authorizations = {@Authorization("ROLE_AUTH_DELIUS_LDAP")})
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_AUTH_DELIUS_LDAP')")
@Validated
public class AuthenticationController {

    private final UserService userService;

    @ApiOperation(
            value = "Authenticate a username and password against Delius Identity (LDAP)",
            authorizations = {@Authorization("ROLE_AUTH_DELIUS_LDAP")})
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class)
            })
    @PostMapping("/authenticate")
    public void authenticate(@NotNull @Valid @ApiParam(value = "Authentication Details", required = true) @RequestBody final AuthUser authUser) {
        if (!userService.authenticateUser(authUser.getUsername(), authUser.getPassword())) {
            throw new UnauthorisedException(String.format("User with username %s", authUser.getUsername()));
        }
    }

    @ApiOperation(
            value = "Find user details of a user held in Delius Identity (LDAP)",
            authorizations = {@Authorization("ROLE_AUTH_DELIUS_LDAP")})
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", response = UserDetails.class),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Not Found", response = ErrorResponse.class)
            })
    @RequestMapping(value = "/users/{username}/details", method = RequestMethod.GET)
    public UserDetails findUser(@ApiParam(name = "username", value = "LDAP username", example = "TESTUSERNPS", required = true) @NotNull final @PathVariable("username") String username) {
        return userService.getUserDetails(username)
                .orElseThrow(() -> new NotFoundException(String.format("User with username %s", username)));
    }

    @ApiOperation(
            value = "Change password a users (LDAP) account",
            authorizations = {@Authorization("ROLE_AUTH_DELIUS_LDAP")})
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Password Changed"),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Not Found", response = ErrorResponse.class)
            })
    @PostMapping(value = "/users/{username}/password")
    public void changePassword(@ApiParam(name = "username", value = "LDAP username", example = "TESTUSERNPS", required = true) @NotNull final @PathVariable("username") String username,
                               @NotNull @Valid @ApiParam(value = "Password Credentials", required = true) @RequestBody final AuthPassword authPassword) {
        if (!userService.changePassword(username, authPassword.getPassword())) {
            throw new NotFoundException(String.format("User with username %s", username));
        }
    }
}
