package uk.gov.justice.digital.delius.ldap.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.ContainerCriteria;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.ldap.repository.entity.NDeliusRole;
import uk.gov.justice.digital.delius.ldap.repository.entity.NDeliusUser;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapName;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Repository
public class LdapRepository {

    private final LdapTemplate ldapTemplate;
    private final LdapTemplate authenticationTemplate;
    @Value("${delius.ldap.users.base}")
    private String ldapUserBase;

    @Autowired
    public LdapRepository(
            @Qualifier(value = "ldapTemplate")
            final LdapTemplate ldapTemplate,
            @Qualifier(value = "authenticationTemplate")
            final LdapTemplate authenticationTemplate) {
        this.ldapTemplate = ldapTemplate;
        this.authenticationTemplate = authenticationTemplate;
    }

    public Optional<String> getDeliusUid(final String distinguishedName) {
        return Optional.ofNullable(ldapTemplate.lookup(distinguishedName,
                (AttributesMapper<String>) attrs -> (String) attrs.get("uid").get()));
    }

    public boolean authenticateUser(final String user, final String password) {
        return authenticationTemplate.authenticate(ldapUserBase, "(cn=" + user + ")", password);
    }

    public Optional<NDeliusUser> getDeliusUser(final String username) {
        // this is a two step process:
        //   1. find the user matching the supplied username in the delius LDAP.
        //   2. find the roles associated with the matched user and add them to the user entity.
        final var nDeliusUser = authenticationTemplate.find(byUsername(username), NDeliusUser.class).stream().findAny();
        return nDeliusUser.map(user -> mapRolesForUser(user));
    }

    public Optional<NDeliusUser> getDeliusUserNoRoles(final String username) {
        return authenticationTemplate.find(byUsername(username), NDeliusUser.class).stream().findAny();
    }

    public List<NDeliusUser> getDeliusUserByEmail(final String email) {
        // this is a two step process:
        //   1. find a list of users matching the supplied email address in the delius LDAP.
        //   2. find the roles associated with the matched users and add them to the user entities.
        final var users = authenticationTemplate.find(byEmail(email), NDeliusUser.class);
        return users.stream()
            .map(user -> mapRolesForUser(user))
            .collect(Collectors.toList());
    }

    public NDeliusUser mapRolesForUser(final NDeliusUser user) {
        // query the delius LDAP for the roles associated with this user and return a new user object with the roles added.
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
                                        .build());

        return user.toBuilder().roles(roles).build();
    }

    public void addRole(String username, String roleId)  {

        String roleContext = getRoleCatalogue()
                .add("cn", roleId)
                .build().toString();

        Attributes attributes = new BasicAttributes(true);
        attributes.put(attribute("objectclass", "NDRoleAssociation", "Alias", "top"));
        attributes.put(attribute("aliasedObjectName", roleContext));
        attributes.put(attribute("cn", roleId));

        LdapName newRoleAssociationContext = LdapNameBuilder.newInstance(ldapUserBase)
                .add("cn", username)
                .add("cn", roleId)
                .build();

        authenticationTemplate.rebind(newRoleAssociationContext, null, attributes);
    }

    private Attribute attribute(String id, String... values) {
        Attribute attribute = new BasicAttribute(id);
        Stream.of(values).forEach(attribute::add);
        return attribute;
    }

    public List<String> getAllRoles() {
        return ldapTemplate.listBindings(
                getRoleCatalogue().build().toString(),
                (ContextMapper<String>) pair ->
                    ((DirContextAdapter)pair).getStringAttribute("cn"));
    }

    private LdapNameBuilder getRoleCatalogue() {
        return LdapNameBuilder.newInstance(ldapUserBase).add("cn=ndRoleCatalogue");
    }

    public boolean changePassword(final String username, final String password) {
        final var context = authenticationTemplate.searchForContext(byUsername(username));

        context.setAttributeValue("userpassword", password);

        authenticationTemplate.modifyAttributes(context);
        return true;
    }

    private ContainerCriteria byEmail(final String email) {
        return query().base(ldapUserBase).where("mail").is(email);
    }

    private ContainerCriteria byUsername(final String username) {
        return query().base(ldapUserBase).where("cn").is(username);
    }

    public String getEmail(final String username) {
        final var nDeliusUser = authenticationTemplate.find(byUsername(username), NDeliusUser.class).stream().findAny();

        return nDeliusUser.map(NDeliusUser::getMail).orElse(null);
    }
}
