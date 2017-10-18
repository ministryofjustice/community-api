package uk.gov.justice.digital.delius.controller;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.user.UserData;

@RestController
@RequestMapping("${base.url:/logon}")
@Log
public class LogonController {

    private final Jwt jwt;

    @Autowired
    public LogonController(Jwt jwt) {
        this.jwt = jwt;
    }

    @RequestMapping(value = "{userId}", method = RequestMethod.POST)
    public String getToken(final @PathVariable("userId") String userId) {
        return jwt.buildToken(UserData.builder().distinguishedName(userId).build());
    }

}
