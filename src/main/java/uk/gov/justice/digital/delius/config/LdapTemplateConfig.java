package uk.gov.justice.digital.delius.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;

import static javax.naming.directory.SearchControls.ONELEVEL_SCOPE;

@Configuration
public class LdapTemplateConfig {

    @Bean(name = "authenticationTemplate")
    public LdapTemplate authenticationTemplate(ContextSource contextSource) {
        var authenticationTemplate = new LdapTemplate(contextSource);
        authenticationTemplate.setDefaultSearchScope(ONELEVEL_SCOPE);
        return authenticationTemplate;
    }

    @Bean(name = "ldapTemplate")
    public LdapTemplate ldapTemplate(ContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }
}
