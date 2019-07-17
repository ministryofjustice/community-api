package uk.gov.justice.digital.delius.ldap.repository;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.*;
import org.springframework.ldap.query.ContainerCriteria;
import org.springframework.ldap.query.SearchScope;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.ldap.repository.entity.NDeliusRole;
import uk.gov.justice.digital.delius.ldap.repository.entity.NDeliusUser;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Repository
@Slf4j
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
        val nDeliusUser = ldapTemplate.findOne(byUsername(username), NDeliusUser.class);

        // TODO search is slow so there should be a better way of finding these alias, filtering didn't seem to work
        // this is probably due to references
        return Optional.ofNullable(nDeliusUser)
                .map(user -> {
                    val roles = ldapTemplate
                            .search(
                                    query()
                                            .base(String.format("cn=%s,%s", nDeliusUser.getCn(), ldapUserBase))
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

    public boolean changePassword(String username, String password) {
        val context = ldapTemplate.searchForContext(byUsername(username));

        context.setAttributeValue("userpassword", password);

        ldapTemplate.modifyAttributes(context);
        return true;
    }

    public boolean lockAccount(String username) {
        val context = ldapTemplate.searchForContext(byUsername(username));

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        context.setAttributeValue("orclActiveEndDate", dateFormat.format(new Date()).concat("Z"));

        ldapTemplate.modifyAttributes(context);
        return true;
    }

    public boolean unlockAccount(String username) {
        val context = ldapTemplate.searchForContext(byUsername(username));

        context.removeAttributeValue("orclActiveEndDate", context.getStringAttribute("orclActiveEndDate"));

        ldapTemplate.modifyAttributes(context);
        return true;
    }

    private ContainerCriteria byUsername(String username) {
        return query().base(ldapUserBase).where("uid").is(username);
    }
}
