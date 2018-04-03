package uk.gov.justice.digital.delius.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.ldap.repository.LdapRepository;
import uk.gov.justice.digital.delius.user.UserData;

import javax.naming.InvalidNameException;
import javax.naming.NameNotFoundException;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/logon")
@Slf4j
@Api( description = "Obtain JWT token", tags = "Logon as user")
public class LogonController {

    public static final String NATIONAL_USER = "NationalUser";
    private final Jwt jwt;
    private final LdapRepository ldapRepository;

    @Autowired
    public LogonController(Jwt jwt, LdapRepository ldapRepository) {
        this.jwt = jwt;
        this.ldapRepository = ldapRepository;
    }

    private ResponseEntity<String> notFound() {
        return new ResponseEntity<>(NOT_FOUND);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "text/plain")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "User lookup: not found")
    })
    public ResponseEntity<String> getToken(final @RequestBody String distinguishedName) {

        log.info("Received call to getToken with body {}", distinguishedName);

        Optional<String> maybeUid = NATIONAL_USER.equals(distinguishedName) ? Optional.of("NationalUser") : ldapRepository.getDeliusUid(distinguishedName);

        return maybeUid.map(uid ->
                new ResponseEntity<>(jwt.buildToken(UserData.builder()
                        .distinguishedName(distinguishedName)
                        .uid(uid)
                        .build()), HttpStatus.OK)).orElse(notFound());
    }

    @ExceptionHandler(InvalidNameException.class)
    public ResponseEntity<String> badDistinguishedName(Exception e) {
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(NameNotFoundException.class)
    public ResponseEntity<String> noSuchDistinguishedName(Exception e) {
        return new ResponseEntity<>(e.getMessage(), NOT_FOUND);
    }
}
