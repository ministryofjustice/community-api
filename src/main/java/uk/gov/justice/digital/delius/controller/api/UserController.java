package uk.gov.justice.digital.delius.controller.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.data.api.UserAndLdap;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.ldap.repository.LdapRepository;
import uk.gov.justice.digital.delius.service.UserService;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.OK;

@RestController
@Slf4j
@Profile("user")
@RequestMapping(value = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final UserService userService;
    private final LdapRepository ldapRepository;

    @Autowired
    public UserController(UserService userService, LdapRepository ldapRepository) {
        this.userService = userService;
        this.ldapRepository = ldapRepository;
    }


    @RequestMapping(value = "/users", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<UserAndLdap>> userSearch(final @RequestHeader HttpHeaders httpHeaders,
                                                        final @RequestParam("surname") @NotNull String surname,
                                                        final @RequestParam("forename") Optional<String> forename) {

        return new ResponseEntity<>(
                userService.getUsersList(surname, forename)
                        .stream()
                        .map(user -> UserAndLdap.builder()
                                .user(user)
                                .ldapMatches(ldapRepository.searchByFieldAndValue("uid", user.getDistinguishedName()))
                                .build())
                        .collect(Collectors.toList()), OK);
    }

    @RequestMapping(value = "/ldap", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<Map<String, String>>> ldapSearch(final @RequestHeader HttpHeaders httpHeaders,
                                                                final @RequestParam("field") @NotNull String field,
                                                                final @RequestParam("value") @NotNull String value) {

        return new ResponseEntity<>(ldapRepository.searchByFieldAndValue(field, value), OK);
    }


}
