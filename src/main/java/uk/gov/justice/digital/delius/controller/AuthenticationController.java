package uk.gov.justice.digital.delius.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.data.api.UserDetails;
import uk.gov.justice.digital.delius.service.UserService;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RestController
@ConditionalOnProperty(
        name = "features.auth.experimental")
public class AuthenticationController {

    private final UserService userService;

    public AuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping("/authenticate")
    public ResponseEntity authenticate(final @RequestParam String username, @RequestParam String password) {
        boolean authenticated = userService.authenticateUser(username, password);
        if(!authenticated) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/users/{username}/details", method = RequestMethod.GET)
    public ResponseEntity<UserDetails> findUser(final @PathVariable("username") String username) {
        return userService.getUserDetails(username).map(userDetails -> new ResponseEntity<>(userDetails, OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));

    }

    @RequestMapping(value = "/users/{username}/password", method = RequestMethod.POST)
    public ResponseEntity changePassword(final @PathVariable("username") String username, final @RequestParam("password") String password) {
        if  (userService.changePassword(username, password)) {
            return new ResponseEntity(OK);
        }
        return new ResponseEntity(NOT_FOUND);

    }

    @RequestMapping(value = "/users/{username}/lock", method = RequestMethod.POST)
    public ResponseEntity lockUsersAccount(final @PathVariable("username") String username) {
        if  (userService.lockAccount(username)) {
            return new ResponseEntity(OK);
        }
        return new ResponseEntity(NOT_FOUND);

    }

    @RequestMapping(value = "/users/{username}/unlock", method = RequestMethod.POST)
    public ResponseEntity unlockUsersAccount(final @PathVariable("username") String username) {
        if  (userService.unlockAccount(username)) {
            return new ResponseEntity(OK);
        }
        return new ResponseEntity(NOT_FOUND);

    }


}