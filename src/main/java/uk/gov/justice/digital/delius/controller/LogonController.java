package uk.gov.justice.digital.delius.controller;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.jpa.entity.User;
import uk.gov.justice.digital.delius.jpa.repository.UserRepository;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.user.UserData;

import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public ResponseEntity<String> getToken(final @RequestBody @NotNull String distinguishedName) {

        Optional<User> maybeUser = userRepository.findByDistinguishedName(deliusUserOf(distinguishedName));

        return maybeUser.map(user -> new ResponseEntity<>(jwt.buildToken(UserData.builder()
                .distinguishedName(distinguishedName)
                .deliusDistinguishedName(user.getDistinguishedName())
                .build()), HttpStatus.OK)).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    private String deliusUserOf(String distinguishedName) {

        Pattern pattern = Pattern.compile("(?:[^=]+=)([^,]+)");

        Matcher matcher = pattern.matcher(distinguishedName);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return distinguishedName;
        }
    }
}
