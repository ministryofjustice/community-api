package uk.gov.justice.digital.delius.ldap.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {

    private final LdapTemplate ldapTemplate;

    @Autowired
    public UserRepository(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public Optional<String> getDeliusUid(String distinguishedName) {
        return Optional.ofNullable(ldapTemplate.lookup(distinguishedName,
                (AttributesMapper<String>) attrs -> (String) attrs.get("uid").get()));

    }
}
