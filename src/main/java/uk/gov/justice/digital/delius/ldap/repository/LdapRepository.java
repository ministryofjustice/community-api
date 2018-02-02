package uk.gov.justice.digital.delius.ldap.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Repository;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Repository
public class LdapRepository {

    private final LdapTemplate ldapTemplate;

    @Autowired
    public LdapRepository(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public Optional<String> getDeliusUid(String distinguishedName) {
        return Optional.ofNullable(ldapTemplate.lookup(distinguishedName,
                (AttributesMapper<String>) attrs -> (String) attrs.get("uid").get()));

    }

    public Map<String, String> getAll(String distinguishedName) {
        return ldapTemplate.lookup(distinguishedName, getMapAttributesMapper());
    }

    private AttributesMapper<Map<String, String>> getMapAttributesMapper() {
        return attrs -> {
            Map<String, String> attrsMap = new HashMap<>();
            NamingEnumeration<? extends Attribute> all = attrs.getAll();
            while (all.hasMore()) {
                Attribute attr = all.next();
                attrsMap.put(attr.getID(), attr.get().toString());
            }
            return attrsMap;
        };
    }

    public List<Map<String, String>> searchByFieldAndValue(String field, String value) {
        return ldapTemplate.search(
                query().where(field).is(value), getMapAttributesMapper());
    }
}
