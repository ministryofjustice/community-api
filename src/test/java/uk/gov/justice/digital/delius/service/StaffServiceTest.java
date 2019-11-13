package uk.gov.justice.digital.delius.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.aStaff;
import static uk.gov.justice.digital.delius.util.EntityHelper.aUser;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffRepository;
import uk.gov.justice.digital.delius.ldap.repository.LdapRepository;
import uk.gov.justice.digital.delius.transformers.ContactTransformer;
import uk.gov.justice.digital.delius.transformers.OffenderTransformer;
import uk.gov.justice.digital.delius.transformers.StaffTransformer;
import uk.gov.justice.digital.delius.transformers.TeamTransformer;
@RunWith(MockitoJUnitRunner.class)
public class StaffServiceTest {

        private StaffService staffService;

        @Mock
        private StaffRepository staffRepository;

        @Mock
        private LdapRepository ldapRepository;

        @Before
        public void setup() {
                staffService = new StaffService(
                        staffRepository,
                        ldapRepository,
                        new OffenderTransformer(new ContactTransformer()),
                        new StaffTransformer(new TeamTransformer()));
        }

        @Test
        public void whenStaffMemberNotFoundReturnEmpty() {
                when(staffRepository.findByOfficerCode("ABC123")).thenReturn(Optional.empty());

                assertThat(staffService.getStaffDetails("ABC123")).isNotPresent();

        }

        @Test
        public void whenStaffMemberFoundReturnStaffDetails() {
                when(staffRepository.findByOfficerCode("ABC123")).thenReturn(Optional.of(aStaff()));

                assertThat(staffService.getStaffDetails("ABC123")).isPresent();
        }

        @Test
        public void whenStaffIsAssociatedWithUserLDAPIsQueried() {
                when(staffRepository.findByOfficerCode("ABC123"))
                        .thenReturn(
                                Optional.of(
                                        aStaff()
                                        .toBuilder()
                                        .user(
                                                aUser()
                                                        .toBuilder()
                                                        .distinguishedName("username")
                                                        .build())
                                        .build()));

                staffService.getStaffDetails("ABC123");

                verify(ldapRepository).getEmail("username");
        }


        @Test
        public void willCopyEmailWhenUserFoundInLDAP() {
                when(staffRepository.findByOfficerCode("ABC123"))
                        .thenReturn(
                                Optional.of(
                                        aStaff()
                                        .toBuilder()
                                        .user(
                                                aUser()
                                                        .toBuilder()
                                                        .distinguishedName("username")
                                                        .build())
                                        .build()));

                when(ldapRepository.getEmail("username")).thenReturn("user@service.com");                  

                assertThat(staffService.getStaffDetails("ABC123")).get().extracting(StaffDetails::getEmail).isEqualTo("user@service.com"); 
        }


        @Test
        public void willSetNullEmailWhenUserNotFoundInLDAP() {
                when(staffRepository.findByOfficerCode("ABC123"))
                        .thenReturn(
                                Optional.of(
                                        aStaff()
                                        .toBuilder()
                                        .user(
                                                aUser()
                                                        .toBuilder()
                                                        .distinguishedName("username")
                                                        .build())
                                        .build()));

                when(ldapRepository.getEmail("username")).thenReturn(null);                  

                assertThat(staffService.getStaffDetails("ABC123")).get().extracting(StaffDetails::getEmail).isNull(); 
        }
}
