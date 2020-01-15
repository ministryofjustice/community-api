package uk.gov.justice.digital.delius.ldap.repository;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class LdapTestConfig {

//    @Bean
//    public InMemoryDirectoryServer directoryServer(ApplicationContext applicationContext) throws LDAPException {
//        String[] baseDn = StringUtils.toStringArray(this.embeddedProperties.getBaseDn());
//        InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig(baseDn);
//        if (hasCredentials(this.embeddedProperties.getCredential())) {
//            config.addAdditionalBindCredentials(this.embeddedProperties.getCredential().getUsername(),
//                    this.embeddedProperties.getCredential().getPassword());
//        }
//        setSchema(config);
//        InMemoryListenerConfig listenerConfig = InMemoryListenerConfig.createLDAPConfig("LDAP",
//                this.embeddedProperties.getPort());
//        config.setListenerConfigs(listenerConfig);
//        this.server = new InMemoryDirectoryServer(config);
//        importLdif(applicationContext);
//        this.server.startListening();
//        setPortProperty(applicationContext, this.server.getListenPort());
//        return this.server;
//    }

}
