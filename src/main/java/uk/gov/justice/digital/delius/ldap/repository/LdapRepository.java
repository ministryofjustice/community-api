package uk.gov.justice.digital.delius.ldap.repository;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.SearchScope;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.ldap.repository.entity.NDeliusRole;
import uk.gov.justice.digital.delius.ldap.repository.entity.NDeliusUser;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Repository
public class LdapRepository {

    private final LdapTemplate ldapTemplate;
    private final ContextMapper<Map<String, String>> contextMapper;
    @Value("${delius.ldap.users.base}")
    private String ldapUserBase;

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

    public boolean authenticateDeliusUser(String user, String password) {
        return ldapTemplate.authenticate(ldapUserBase, "(uid=" + user + ")", password);
    }

    public Optional<NDeliusUser> getDeliusUser(String username) {
        val nDeliusUser = ldapTemplate.findOne(query().base("cn=Users,dc=moj,dc=com").where("uid").is(username), NDeliusUser.class);

        // TODO search is slow so there should be a better way of finding these alias, filtering didn't seem to work
        return Optional.ofNullable(nDeliusUser)
                .map(user -> {
                    val roles = ldapTemplate
                            .search(
                                    query()
                                            .base(String.format("cn=%s,cn=Users,dc=moj,dc=com", nDeliusUser.getCn()))
                                            .searchScope(SearchScope.ONELEVEL)
                                            .filter("(objectClass=*)"),
                                    (AttributesMapper<Optional<NDeliusRole>>) attributes ->
                                            attributes.get("objectclass").contains("NDRole") ?
                                                    Optional.of(NDeliusRole
                                                            .builder()
                                                            .cn(attributes.get("cn").get().toString())
                                                            .description(attributes.get("description").get().toString())
                                                            .build()) :
                                                    Optional.empty());

                    return nDeliusUser.toBuilder().roles(roles
                            .stream()
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(toList())).build();

                });

    }
}
