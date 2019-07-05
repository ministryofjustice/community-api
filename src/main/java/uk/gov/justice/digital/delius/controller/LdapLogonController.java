package uk.gov.justice.digital.delius.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.ldap.repository.LdapRepository;

@RestController
public class LdapLogonController {

    private final LdapRepository ldapRepository;

    public LdapLogonController(LdapRepository ldapRepository) {
        this.ldapRepository = ldapRepository;
    }

    @RequestMapping("/ldaplogon")
    public ResponseEntity logonWithLdap(final @RequestParam String username, @RequestParam String password) {
        boolean authenticated = ldapRepository.authenticateDeliusUser(username, password);
        if(!authenticated) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.OK);
    }
}