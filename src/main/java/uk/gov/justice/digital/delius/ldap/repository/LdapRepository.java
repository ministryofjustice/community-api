package uk.gov.justice.digital.delius.ldap.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
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
    private final ContextMapper<Map<String, String>> contextMapper;

    @Autowired
    public LdapRepository(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
        contextMapper = getContextMapper();
    }

    public Optional<String> getDeliusUid(String distinguishedName) {
        return Optional.ofNullable(ldapTemplate.lookup(distinguishedName,
                (AttributesMapper<String>) attrs -> (String) attrs.get("uid").get()));

    }

    private ContextMapper<Map<String, String>> getContextMapper() {

        return ctx -> {
            DirContextAdapter context = (DirContextAdapter) ctx;

            Map<String, String> attrsMap = new HashMap<>();
            NamingEnumeration<? extends Attribute> all = context.getAttributes().getAll();
            while (all.hasMore()) {
                Attribute attr = all.next();
                attrsMap.put(attr.getID(), attr.get().toString());
            }

            attrsMap.put("dn", context.getDn().toString());

            return attrsMap;
        };
    }

    public List<Map<String, String>> searchByFieldAndValue(String field, String value) {
        return ldapTemplate.search(query().where(field).is(value), contextMapper);
    }
}
