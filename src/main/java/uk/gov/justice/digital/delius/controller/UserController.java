package uk.gov.justice.digital.delius.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.data.api.UsersAndLdap;
import uk.gov.justice.digital.delius.exception.JwtTokenMissingException;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.ldap.repository.LdapRepository;
import uk.gov.justice.digital.delius.service.UserService;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.OK;

@RestController
@Slf4j
@Profile("user")
public class UserController {

    private final UserService userService;
    private final LdapRepository ldapRepository;
    private final Jwt jwt;

    @Autowired
    public UserController(UserService userService, LdapRepository ldapRepository, Jwt jwt) {
        this.userService = userService;
        this.ldapRepository = ldapRepository;
        this.jwt = jwt;
    }


    @RequestMapping(value = "/users", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<UsersAndLdap> userSearch(final @RequestHeader HttpHeaders httpHeaders,
                                                                final @RequestParam("surname") @NotNull String surname,
                                                                final @RequestParam("forename") Optional<String> forename) {

        Claims claims = jwt.parseAuthorizationHeader(httpHeaders.getFirst(HttpHeaders.AUTHORIZATION)).get();

        return new ResponseEntity<>(
                UsersAndLdap.builder()
                        .users(userService.getUsersList(surname, forename))
                        .ldapEntryFromProvidedJwt(ldapRepository.getAll(claims.getSubject()))
                        .build(), OK);
    }

    @RequestMapping(value = "/ldap", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<Map<String, String>>> ldapSearch(final @RequestHeader HttpHeaders httpHeaders,
                                                                             final @RequestParam("field") @NotNull String field,
                                                                             final @RequestParam("value") @NotNull String value) {

        return new ResponseEntity<>(ldapRepository.searchByFieldAndValue(field, value), OK);
    }

    @ExceptionHandler(JwtTokenMissingException.class)
    public ResponseEntity<String> missingJwt(JwtTokenMissingException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<String> badJwt(MalformedJwtException e) {
        return new ResponseEntity<>("Bad Token.", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<String> expiredJwt(ExpiredJwtException e) {
        return new ResponseEntity<>("Expired Token.", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<String> notMine(SignatureException e) {
        return new ResponseEntity<>("Invalid signature.", HttpStatus.FORBIDDEN);
    }


}
