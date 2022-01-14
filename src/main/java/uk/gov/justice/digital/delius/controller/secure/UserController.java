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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.UserDetails;
import uk.gov.justice.digital.delius.service.UserService;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Api(tags = "Users")
@Validated
public class UserController {

    private final UserService userService;

    @ApiOperation(
        value = "Find user details of a user held in Delius Identity (LDAP)",
        authorizations = {@Authorization("ROLE_COMMUNITY_AUTH_INT,ROLE_COMMUNITY_USERS,ROLE_COMMUNITY_USERS_ROLES")})
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Requires any of ROLE_COMMUNITY_AUTH_INT,ROLE_COMMUNITY_USERS,ROLE_COMMUNITY_USERS_ROLES"),
            @ApiResponse(code = 404, message = "Not Found", response = ErrorResponse.class)
        })
    @PreAuthorize("hasAnyRole('ROLE_COMMUNITY_AUTH_INT','ROLE_COMMUNITY_USERS','ROLE_COMMUNITY_USERS_ROLES')")
    @RequestMapping(value = "/users/{username}/details", method = RequestMethod.GET)
    public UserDetails findUser(@ApiParam(name = "username", value = "LDAP username", example = "TESTUSERNPS", required = true) @NotNull final @PathVariable("username") String username) {
        return userService.getUserDetails(username)
            .orElseThrow(() -> new NotFoundException(String.format("User with username %s", username)));
    }

    @ApiOperation(
        value = "Find user details of a user held in Delius Identity (LDAP)",
        authorizations = {@Authorization("ROLE_COMMUNITY_USERS")})
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY_USERS"),
        })
    @PreAuthorize("hasAnyRole('ROLE_COMMUNITY_AUTH_INT','ROLE_COMMUNITY_USERS')")
    @RequestMapping(value = "/users/search/email/{email}/details", method = RequestMethod.GET)
    public List<UserDetails> findUserByEmail(@ApiParam(name = "email", value = "LDAP email address", example = "sheila.hancock@justice.gov.uk", required = true) @NotNull final @PathVariable("email") String email) {
        return userService.getUserDetailsByEmail(email);
    }

    @ApiOperation(
        value = "Add a role to a user held in Delius Identity (LDAP)",
        authorizations = {@Authorization("ROLE_COMMUNITY_USERS_ROLES")})
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY_USERS_ROLES"),
            @ApiResponse(code = 404, message = "Not Found", response = ErrorResponse.class)
        })
    @PreAuthorize("hasAnyRole('ROLE_COMMUNITY_USERS_ROLES')")
    @RequestMapping(value = "/users/{username}/roles/{roleId}", method = RequestMethod.PUT)
    public void addRole(
        @ApiParam(name = "username", value = "LDAP username", example = "TESTUSERNPS", required = true) @NotNull final @PathVariable("username") String username,
        @ApiParam(name = "roleId", value = "Delius Role ID", example = "CWBT001", required = true) @NotNull final @PathVariable("roleId") String roleId
    ) {
        userService.addRole(username, roleId);
    }
}
