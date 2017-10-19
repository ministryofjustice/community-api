package uk.gov.justice.digital.delius.ldap.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Repository
public class UserRepository {

    private final LdapTemplate ldapTemplate;

    @Autowired
    public UserRepository(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

//    public List<String> getAllUserNames() {
//        return ldapTemplate.search(
//                query().where("objectclass").is("person"),
//                (AttributesMapper<String>) attrs -> (String) attrs.get("cn").get());
//    }

    public Optional<String> getOracleUser(String distinguishedName) {
        return Optional.ofNullable(ldapTemplate.lookup(distinguishedName,
                (AttributesMapper<String>) attrs -> (String) attrs.get("oracleUser").get()));

    }


}
