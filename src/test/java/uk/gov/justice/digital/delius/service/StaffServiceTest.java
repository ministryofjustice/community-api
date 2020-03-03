package uk.gov.justice.digital.delius.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static uk.gov.justice.digital.delius.util.EntityHelper.*;

import java.util.*;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.data.api.Human;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffHelperRepository;
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

    @Mock
    private StaffHelperRepository staffHelperRepository;

    @Captor
    private ArgumentCaptor<Staff> staffCaptor;

    @Before
    public void setup() {
        staffService = new StaffService(
                staffRepository,
                ldapRepository,
                new OffenderTransformer(new ContactTransformer()),
                new StaffTransformer(new TeamTransformer()),
                staffHelperRepository);
    }

    @Test
    public void whenStaffMemberNotFoundReturnEmpty_getStaffDetails() {
        when(staffRepository.findByOfficerCode("ABC123")).thenReturn(Optional.empty());

        assertThat(staffService.getStaffDetails("ABC123")).isNotPresent();

    }

    @Test
    public void whenStaffMemberFoundReturnStaffDetails_getStaffDetails() {
        when(staffRepository.findByOfficerCode("ABC123")).thenReturn(Optional.of(aStaff()));

        assertThat(staffService.getStaffDetails("ABC123")).isPresent();
    }

    @Test
    public void whenStaffIsAssociatedWithUserLDAPIsQueried_getStaffDetails() {
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
    public void willCopyEmailWhenUserFoundInLDAP_getStaffDetails() {
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
    public void willSetNullEmailWhenUserNotFoundInLDAP_getStaffDetails() {
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

    @Test
    public void whenStaffMemberNotFoundReturnEmpty_getStaffDetailsByUsername() {
        when(staffRepository.findByUsername("sandrasmith")).thenReturn(Optional.empty());

        assertThat(staffService.getStaffDetailsByUsername("sandrasmith")).isNotPresent();

    }

    @Test
    public void whenStaffMemberFoundReturnStaffDetails_getStaffDetailsByUsername() {
        when(staffRepository.findByUsername("sandrasmith")).thenReturn(Optional.of(aStaff()));

        assertThat(staffService.getStaffDetailsByUsername("sandrasmith")).isPresent();
    }

    @Test
    public void willCopyEmailWhenUserFoundInLDAP_getStaffDetailsByUsername() {
        when(staffRepository.findByUsername("sandrasmith"))
                .thenReturn(
                        Optional.of(
                                aStaff()
                                        .toBuilder()
                                        .user(
                                                aUser()
                                                        .toBuilder()
                                                        .distinguishedName("sandrasmith")
                                                        .build())
                                        .build()));

        when(ldapRepository.getEmail("sandrasmith")).thenReturn("user@service.com");

        assertThat(staffService.getStaffDetailsByUsername("sandrasmith")).get().extracting(StaffDetails::getEmail).isEqualTo("user@service.com");
    }

    @Test
    public void willSetNullEmailWhenUserNotFoundInLDAP_getStaffDetailsByUsername() {
        when(staffRepository.findByUsername("sandrasmith"))
                .thenReturn(
                        Optional.of(
                                aStaff()
                                        .toBuilder()
                                        .user(
                                                aUser()
                                                        .toBuilder()
                                                        .distinguishedName("sandrasmith")
                                                        .build())
                                        .build()));

        when(ldapRepository.getEmail("sandrasmith")).thenReturn(null);

        assertThat(staffService.getStaffDetailsByUsername("sandrasmith")).get().extracting(StaffDetails::getEmail).isNull();
    }

    @Test
    public void willReturnCorrectDetailsForMultipleUsernames_getStaffDetailsByUsernames() {
        Set usernames = Set.of("joefrazier", "georgeforeman");

        when(staffRepository.findByUsernames(any()))
                .thenReturn(ImmutableList.of(
                        aStaff()
                                .toBuilder()
                                .user(
                                        aUser()
                                                .toBuilder()
                                                .distinguishedName("joefrazier")
                                                .build())
                                .build(),
                        aStaff()
                                .toBuilder()
                                .user(
                                        aUser()
                                                .toBuilder()
                                                .distinguishedName("georgeforeman")
                                                .build())
                                .build()
                ));

        when(ldapRepository.getEmail("joefrazier")).thenReturn("joefrazier@service.com");
        when(ldapRepository.getEmail("georgeforeman")).thenReturn("georgeforeman@service.com");


        List<StaffDetails> staffDetailsList = staffService.getStaffDetailsByUsernames(usernames);

        var frazierUserDetails = staffDetailsList.stream().filter(s -> s.getUsername().equals("joefrazier")).findFirst().get();
        var foremanUserDetails = staffDetailsList.stream().filter(s -> s.getUsername().equals("georgeforeman")).findFirst().get();

        assertThat(staffDetailsList.size()).isEqualTo(2);
        assertThat(frazierUserDetails.getEmail()).isEqualTo("joefrazier@service.com");
        assertThat(foremanUserDetails.getEmail()).isEqualTo("georgeforeman@service.com");
        assertThat(frazierUserDetails.getUsername()).isEqualTo("joefrazier");
        assertThat(foremanUserDetails.getUsername()).isEqualTo("georgeforeman");
    }

    @Test
    public void willReturnStaffIfFoundWithoutCreatingANewOne() {
        when(staffRepository
                .findBySurnameAndForenameAndProbationArea(any(), any(), any(ProbationArea.class)))
                .thenReturn(Optional.of(aStaff("N01A123456")));

        assertThat(staffService.findOrCreateStaffInArea(Human
                .builder()
                .forenames("Sandra")
                .surname("Biggins")
                .build(), aProbationArea()).getOfficerCode()).isEqualTo("N01A123456");

        verify(staffRepository).findBySurnameAndForenameAndProbationArea(eq("Biggins"), eq("Sandra"), isA(ProbationArea.class));
        verify(staffRepository, never()).save(any());
    }

    @Test
    public void willLookupByFirstForenameWhenSpaceSeparatedWhenSearchingStaffByName() {
        when(staffRepository
                .findBySurnameAndForenameAndProbationArea(any(), any(), any(ProbationArea.class)))
                .thenReturn(Optional.of(aStaff("N01A123456")));

        assertThat(staffService.findOrCreateStaffInArea(Human
                .builder()
                .forenames("Sandra Jane")
                .surname("Biggins")
                .build(), aProbationArea()).getOfficerCode()).isEqualTo("N01A123456");

        verify(staffRepository).findBySurnameAndForenameAndProbationArea(eq("Biggins"), eq("Sandra"), isA(ProbationArea.class));
        verify(staffRepository, never()).save(any());
    }

    @Test
    public void willLookupByFirstForenameWhenCommaSeparatedWhenSearchingStaffByName() {
        when(staffRepository
                .findBySurnameAndForenameAndProbationArea(any(), any(), any(ProbationArea.class)))
                .thenReturn(Optional.of(aStaff("N01A123456")));

        assertThat(staffService.findOrCreateStaffInArea(Human
                .builder()
                .forenames("Sandra, Jane")
                .surname("Biggins")
                .build(), aProbationArea()).getOfficerCode()).isEqualTo("N01A123456");

        verify(staffRepository).findBySurnameAndForenameAndProbationArea(eq("Biggins"), eq("Sandra"), isA(ProbationArea.class));
        verify(staffRepository, never()).save(any());
    }

    @Test
    public void willGenerateNewStaffCodeCreateNewStaffWhenNotFoundByName( ) {
        when(staffRepository
                .findBySurnameAndForenameAndProbationArea(any(), any(), any(ProbationArea.class)))
                .thenReturn(Optional.empty());
        when(staffHelperRepository.getNextStaffCode("N02")).thenReturn("N02A123456");
        when(staffRepository.save(any())).then(params -> params.getArgument(0));

        assertThat(staffService.findOrCreateStaffInArea(
                Human
                        .builder()
                        .forenames("Sandra")
                        .surname("Biggins")
                        .build(),
                aProbationArea()
                        .toBuilder()
                        .code("N02")
                        .build()).getOfficerCode()).isEqualTo("N02A123456");
    }

    @Test
    public void willSetStaffNamesWhenCreateNewStaffWhenNotFoundByName( ) {
        when(staffRepository
                .findBySurnameAndForenameAndProbationArea(any(), any(), any(ProbationArea.class)))
                .thenReturn(Optional.empty());
        when(staffHelperRepository.getNextStaffCode(any())).thenReturn("N02A123456");

        staffService.findOrCreateStaffInArea(
                Human
                        .builder()
                        .forenames("Sandra Jane")
                        .surname("Biggins")
                        .build(),
                aProbationArea()
                        .toBuilder()
                        .code("N02")
                        .build());

        verify(staffRepository).save(staffCaptor.capture());

        assertThat(staffCaptor.getValue().getSurname()).isEqualTo("Biggins");
        assertThat(staffCaptor.getValue().getForename()).isEqualTo("Sandra");
    }

    @Test
    public void willSetProbationAreaWhenCreateNewStaffWhenNotFoundByName( ) {
        when(staffRepository
                .findBySurnameAndForenameAndProbationArea(any(), any(), any(ProbationArea.class)))
                .thenReturn(Optional.empty());
        when(staffHelperRepository.getNextStaffCode(any())).thenReturn("N02A123456");

        staffService.findOrCreateStaffInArea(
                Human
                        .builder()
                        .forenames("Sandra Jane")
                        .surname("Biggins")
                        .build(),
                aProbationArea()
                        .toBuilder()
                        .code("C02")
                        .privateSector(1L)
                        .build());

        verify(staffRepository).save(staffCaptor.capture());

        assertThat(staffCaptor.getValue().getProbationArea().getCode()).isEqualTo("C02");
        assertThat(staffCaptor.getValue().getPrivateSector()).isEqualTo(1L);
    }
}
