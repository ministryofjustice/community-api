package uk.gov.justice.digital.delius.ldap.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
public class LdapRepositoryTest {
    @Autowired
    private LdapRepository ldapRepository;

    @Test
    public void shouldFindEmailAddressForUser() {
        assertThat(ldapRepository.getEmail("SheilaHancockNPS"))
                .isEqualTo("sheila.hancock@justice.gov.uk");
    }
    @Test
    public void shouldReturnNullForUserFoundButNoEmail() {
        assertThat(ldapRepository.getEmail("UserWithNoEmail"))
                .isNull();
    }
    @Test
    public void shouldReturnNullForUserNotFound() {
        assertThat(ldapRepository.getEmail("EmailNotPresentNPS"))
                .isNull();
    }
    
}