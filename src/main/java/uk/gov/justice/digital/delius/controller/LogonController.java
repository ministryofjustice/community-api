package uk.gov.justice.digital.delius.controller;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.ldap.repository.UserRepository;
import uk.gov.justice.digital.delius.user.UserData;

import javax.naming.InvalidNameException;
import javax.naming.NameNotFoundException;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/logon")
@Log
public class LogonController {

    private final Jwt jwt;
    private final UserRepository userRepository;

    @Autowired
    public LogonController(Jwt jwt, UserRepository userRepository) {
        this.jwt = jwt;
        this.userRepository = userRepository;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "text/plain")
    public ResponseEntity<String> getToken(final @RequestBody String distinguishedName) {

        Optional<String> maybeUid = userRepository.getDeliusUid(distinguishedName);

        return maybeUid.map(uid ->
                new ResponseEntity<>(jwt.buildToken(UserData.builder()
                        .distinguishedName(distinguishedName)
                        .uid(uid)
                        .build()), HttpStatus.OK)).orElse(notFound());
    }

    private ResponseEntity<String> notFound() {
        return new ResponseEntity<>(NOT_FOUND);
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
