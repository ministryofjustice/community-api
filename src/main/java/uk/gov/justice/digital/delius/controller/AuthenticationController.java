package uk.gov.justice.digital.delius.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.data.api.UserDetails;
import uk.gov.justice.digital.delius.ldap.repository.LdapRepository;
import uk.gov.justice.digital.delius.service.UserService;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RestController
public class AuthenticationController {

    private final LdapRepository ldapRepository;
    private final UserService userService;

    public AuthenticationController(LdapRepository ldapRepository, UserService userService) {
        this.ldapRepository = ldapRepository;
        this.userService = userService;
    }

    @RequestMapping("/authenticate")
    public ResponseEntity authenticate(final @RequestParam String username, @RequestParam String password) {
        boolean authenticated = ldapRepository.authenticateDeliusUser(username, password);
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


}