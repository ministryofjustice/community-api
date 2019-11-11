package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.UserDetails;
import uk.gov.justice.digital.delius.service.UserService;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@Api(tags = "Authentication", authorizations = {@Authorization("ROLE_AUTH_DELIUS_LDAP")}, description = "Authentication for Delius Identity (LDAP)")
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_AUTH_DELIUS_LDAP')")
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
    @RequestMapping("/authenticate")
    public ResponseEntity authenticate(@ApiParam(name = "username", value = "LDAP username", example = "TESTUSERNPS", required = true) final @RequestParam String username,
                                       @ApiParam(name = "password", value = "LDAP password", example = "hello123456", required = true) final @RequestParam String password) {
        boolean authenticated = userService.authenticateUser(username, password);
        if(!authenticated) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.OK);
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
    public ResponseEntity<UserDetails> findUser(@ApiParam(name = "username", value = "LDAP username", example = "TESTUSERNPS", required = true)  final @PathVariable("username") String username) {
        return userService.getUserDetails(username).map(userDetails -> new ResponseEntity<>(userDetails, OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
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
    @RequestMapping(value = "/users/{username}/password", method = RequestMethod.POST)
    public ResponseEntity changePassword(@ApiParam(name = "username", value = "LDAP username", example = "TESTUSERNPS", required = true) final @PathVariable("username") String username,
                                         @ApiParam(name = "password", value = "LDAP new password", example = "newpassword123", required = true) final @RequestParam("password") String password) {
        if  (userService.changePassword(username, password)) {
            return new ResponseEntity(OK);
        }
        return new ResponseEntity(NOT_FOUND);
    }

    @ApiOperation(
            value = "Lock a users (LDAP) account",
            authorizations = {@Authorization("ROLE_AUTH_DELIUS_LDAP")})
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Not Found", response = ErrorResponse.class)
            })
    @RequestMapping(value = "/users/{username}/lock", method = RequestMethod.POST)
    public ResponseEntity lockUsersAccount(@ApiParam(name = "username", value = "LDAP username", example = "TESTUSERNPS", required = true) final @PathVariable("username") String username) {
        if  (userService.lockAccount(username)) {
            return new ResponseEntity(OK);
        }
        return new ResponseEntity(NOT_FOUND);
    }

    @ApiOperation(
            value = "Unlock a users (LDAP) account by username",
            authorizations = {@Authorization("ROLE_AUTH_DELIUS_LDAP")})
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Not Found", response = ErrorResponse.class)
            })
    @RequestMapping(value = "/users/{username}/unlock", method = RequestMethod.POST)
    public ResponseEntity unlockUsersAccount(@ApiParam(name = "username", value = "LDAP username", example = "TESTUSERNPS", required = true) final @PathVariable("username") String username) {
        if  (userService.unlockAccount(username)) {
            return new ResponseEntity(OK);
        }
        return new ResponseEntity(NOT_FOUND);
    }

}