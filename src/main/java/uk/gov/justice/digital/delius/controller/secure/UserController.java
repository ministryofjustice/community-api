package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.UserAreas;
import uk.gov.justice.digital.delius.data.api.UserDetailsWrapper;
import uk.gov.justice.digital.delius.service.UserService;

import javax.validation.constraints.NotNull;
import java.util.Set;

@RestController
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "Authentication and users", authorizations = {@Authorization("ROLE_AUTH_DELIUS_LDAP")})
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_AUTH_DELIUS_LDAP')")
@Slf4j
public class UserController {

    private final UserService userService;

    @ApiOperation(value = "Returns a list of user details for supplied usernames - POST version to allow large user lists.", notes = "user details for supplied usernames",
                  nickname = "getUserDetailsList")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Requires role ROLE_AUTH_DELIUS_LDAP"),
            @ApiResponse(code = 404, message = "Not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)})
    @PostMapping(path="/users/list/detail", consumes = "application/json")
    public UserDetailsWrapper getUserDetailsList(final @RequestBody Set<String> usernames){
        return userService.getUserDetailsList(usernames);
    }

    @ApiOperation(value = "Returns a list of areas a user is related to including their home area", nickname = "getUserAreas")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "User with username not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)})
    @RequestMapping(value = "/users/{username}/areas", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_COMMUNITY')")
    public UserAreas getUserAreas(@ApiParam(name = "username", value = "username", example = "johnsmithnps", required = true) @NotNull final @PathVariable("username") String username) {
        return userService.getUserAreas(username)
                .orElseThrow(() -> new NotFoundException(String.format("User with username %s", username)));
    }
}
