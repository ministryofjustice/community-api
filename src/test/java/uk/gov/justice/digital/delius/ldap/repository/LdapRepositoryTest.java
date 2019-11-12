package uk.gov.justice.digital.delius.ldap.repository;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
    "delius.ldap.users.base=ou=people,dc=memorynotfound,dc=com"
})
public class LdapRepositoryTest {
    @Autowired
    private LdapRepository ldapRepository;

    @Test
    public void shoudFindEmailAddressForUser() {
        assertThat(ldapRepository.getEmail("SheilaHancockNPS"))
                .isEqualTo("sheila.hancock@justice.gov.uk");
    }
    @Test
    public void shoudReturnNullForUserFoundButNoEmail() {
        assertThat(ldapRepository.getEmail("UserWithNoEmail"))
                .isNull();
    }
    @Test
    public void shoudReturnNullForUserNotFound() {
        assertThat(ldapRepository.getEmail("UserNotFoundNPS"))
                .isNull();
    }
    
}