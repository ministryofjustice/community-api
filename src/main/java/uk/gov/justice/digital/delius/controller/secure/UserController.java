package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.service.UserService;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "User API", authorizations = {@Authorization("ROLE_COMMUNITY")})
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
@Slf4j
public class UserController {

    private final UserService userService;

    @ApiOperation(value = "Returns a map of user details for supplied usernames - POST version to allow large user lists.", notes = "user details for supplied usernames", nickname = "getUserDetailsMap")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = StaffDetails.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)})
    @PostMapping(path="/users/list/detail", consumes = "application/json")
    public Map getUserDetailsMap(final @RequestBody Set<String> usernames){
        log.info("getUserDetailsMap called with {}", usernames);
        return userService.getUserDetailsMap(usernames);
    }
}
