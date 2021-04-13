package uk.gov.justice.digital.delius.ldap.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.justice.digital.delius.ldap.repository.entity.NDeliusRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static org.springframework.test.annotation.DirtiesContext.MethodMode.AFTER_METHOD;

@SpringBootTest(webEnvironment = NONE)
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

    @Test
    public void shouldReturnListForEmptyEmailSearchResult() {
        assertThat(ldapRepository.getDeliusUserByEmail("missing").size())
            .isEqualTo(0);
    }

    @Test
    public void shouldReturnListForDuplicateEmails() {
        assertThat(ldapRepository.getDeliusUserByEmail("sheila.hancock@justice.gov.uk").size())
            .isEqualTo(2);
    }

    @Test
    public void shouldReturnAttributesForUser() {
        var deliusUser = ldapRepository.getDeliusUserNoRoles("JimSnowLdap").get();

        assertThat(deliusUser.getTelephoneNumber()).isEqualTo("01512112121");
        assertThat(deliusUser.getMail()).isEqualTo("jim.snow@justice.gov.uk");
        assertThat(deliusUser.getSn()).isEqualTo("Snow");
        assertThat(deliusUser.getGivenname()).isEqualTo("Jim");
        assertThat(deliusUser.getRoles()).isNull();
    }

    @Test
    @DirtiesContext(methodMode = AFTER_METHOD)
    public void shouldBeAbleToAddARole() {
        assertThat(ldapRepository.getDeliusUser("bernard.beaks").get().getRoles())
                .extracting(NDeliusRole::getCn)
                .containsExactly("UWBT060");

        ldapRepository.addRole("bernard.beaks", "CWBT001");

        assertThat(ldapRepository.getDeliusUser("bernard.beaks").get().getRoles())
                .extracting(NDeliusRole::getCn)
                .containsExactly("CWBT001", "UWBT060");
    }

    @Test
    public void canRetrieveAllRoles() {
        assertThat(ldapRepository.getAllRoles()).containsOnly("CWBT001", "CWBT001a", "UWBT060");
    }

    @Test
    @DirtiesContext(methodMode = AFTER_METHOD)
    public void cannotAddTheSameRoleTwice() {
        assertThat(ldapRepository.getDeliusUser("bernard.beaks").get().getRoles())
                .extracting(NDeliusRole::getCn)
                .containsExactly("UWBT060");

        ldapRepository.addRole("bernard.beaks", "CWBT001");
        ldapRepository.addRole("bernard.beaks", "CWBT001");

        assertThat(ldapRepository.getDeliusUser("bernard.beaks").get().getRoles())
                .extracting(NDeliusRole::getCn)
                .containsExactly("CWBT001", "UWBT060");
    }

    @Test
    public void addingRoleToNonExistentUserThrowsNamingException() {
        assertThatThrownBy(() -> ldapRepository.addRole("does not exist", "CWBT001"))
            .isInstanceOf(NameNotFoundException.class);
    }

}
