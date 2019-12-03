package uk.gov.justice.digital.delius.ldap.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.ContainerCriteria;
import org.springframework.ldap.query.SearchScope;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.ldap.repository.entity.NDeliusRole;
import uk.gov.justice.digital.delius.ldap.repository.entity.NDeliusUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Repository
public class LdapRepository {

    private final LdapTemplate ldapTemplate;
    private final ContextMapper<Map<String, String>> contextMapper;
    @Value("${delius.ldap.users.base}")
    private String ldapUserBase;

    @Autowired
    public LdapRepository(final LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
        contextMapper = getContextMapper();
    }

    public Optional<String> getDeliusUid(final String distinguishedName) {
        return Optional.ofNullable(ldapTemplate.lookup(distinguishedName,
                (AttributesMapper<String>) attrs -> (String) attrs.get("uid").get()));
    }

    private ContextMapper<Map<String, String>> getContextMapper() {

        return ctx -> {
            final var context = (DirContextAdapter) ctx;

            final Map<String, String> attrsMap = new HashMap<>();
            final var all = context.getAttributes().getAll();
            while (all.hasMore()) {
                final var attr = all.next();
                attrsMap.put(attr.getID(), attr.get().toString());
            }

            attrsMap.put("dn", context.getDn().toString());

            return attrsMap;
        };
    }

    public List<Map<String, String>> searchByFieldAndValue(final String field, final String value) {
        return ldapTemplate.search(query().where(field).is(value), contextMapper);
    }

    public boolean authenticateUser(final String user, final String password) {
        return ldapTemplate.authenticate(ldapUserBase, "(uid=" + user + ")", password);
    }

    public Optional<NDeliusUser> getDeliusUser(final String username) {
        final var nDeliusUser = ldapTemplate.find(byUsername(username), NDeliusUser.class).stream().findAny();

        return nDeliusUser.map(user -> {
            final var roles = ldapTemplate
                    .search(
                            query()
                                    .base(user.getDn())
                                    .searchScope(SearchScope.ONELEVEL)
                                    .filter("(|(objectclass=NDRole)(objectclass=NDRoleAssociation))"),
                            (AttributesMapper<NDeliusRole>) attributes ->
                                    NDeliusRole
                                            .builder()
                                            .cn(attributes.get("cn").get().toString())
                                            .description(attributes.get("description").get().toString())
                                            .build());

            return user.toBuilder().roles(roles).build();
        });
    }

    public boolean changePassword(final String username, final String password) {
        final var context = ldapTemplate.searchForContext(byUsername(username));

        context.setAttributeValue("userpassword", password);

        ldapTemplate.modifyAttributes(context);
        return true;
    }

    private ContainerCriteria byUsername(final String username) {
        return query().base(ldapUserBase).where("uid").is(username);
    }

    public String getEmail(final String username) {
        final var nDeliusUser = ldapTemplate.find(byUsername(username), NDeliusUser.class).stream().findAny();

        return nDeliusUser.map(NDeliusUser::getMail).orElse(null);
    }
}
