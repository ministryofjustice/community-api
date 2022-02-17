package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uk.gov.justice.digital.delius.data.api.ContactableHuman;
import uk.gov.justice.digital.delius.data.api.StaffCaseloadEntry;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffHelperRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffRepository;
import uk.gov.justice.digital.delius.ldap.repository.LdapRepository;
import uk.gov.justice.digital.delius.ldap.repository.entity.NDeliusUser;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.aProbationArea;
import static uk.gov.justice.digital.delius.util.EntityHelper.aStaff;
import static uk.gov.justice.digital.delius.util.EntityHelper.aUser;

@ExtendWith(MockitoExtension.class)
public class StaffServiceTest {

    private StaffService staffService;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private LdapRepository ldapRepository;

    @Mock
    private StaffHelperRepository staffHelperRepository;

    @Mock
    private OffenderRepository offenderRepository;

    @Captor
    private ArgumentCaptor<Staff> staffCaptor;

    @BeforeEach
    public void setup() {
        staffService = new StaffService(
                staffRepository,
                ldapRepository,
                staffHelperRepository,
                offenderRepository);
    }

    @Test
    public void whenStaffMemberNotFoundReturnEmpty_getStaffDetailsByStaffIdentifier() {
        when(staffRepository.findByStaffId(1L)).thenReturn(Optional.empty());

        assertThat(staffService.getStaffDetailsByStaffIdentifier(1L)).isNotPresent();
    }

    @Test
    public void whenStaffMemberFoundReturnStaffDetails_getStaffDetailsByStaffIdentifier() {
        when(staffRepository.findByStaffId(10L)).thenReturn(Optional.of(aStaff()));

        assertThat(staffService.getStaffDetailsByStaffIdentifier(10L)).isPresent();
    }

    @Test
    public void whenStaffIsAssociatedWithUserLDAPIsQueried_getStaffDetailsByStaffIdentifier() {
        when(staffRepository.findByStaffId(10L))
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

        staffService.getStaffDetailsByStaffIdentifier(10L);

        verify(ldapRepository).getEmail("username");
    }

    @Test
    public void willCopyEmailWhenUserFoundInLDAP_getStaffDetailsByStaffIdentifier() {
        when(staffRepository.findByStaffId(10L))
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

        assertThat(staffService.getStaffDetailsByStaffIdentifier(10L)).get().extracting(StaffDetails::getEmail).isEqualTo("user@service.com");
    }

    @Test
    public void willSetNullEmailWhenUserNotFoundInLDAP_getStaffDetailsByStaffIdentifier() {
        when(staffRepository.findByStaffId(10L))
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

        assertThat(staffService.getStaffDetailsByStaffIdentifier(10L)).get().extracting(StaffDetails::getEmail).isNull();
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

        var nDeliusUser = NDeliusUser.builder().mail("user@service.com").build();
        when(ldapRepository.getDeliusUserNoRoles("sandrasmith")).thenReturn(Optional.of(nDeliusUser));

        var staffDetails = staffService.getStaffDetailsByUsername("sandrasmith").get();
        assertThat(staffDetails.getEmail()).isEqualTo("user@service.com");
        assertThat(staffDetails.getTelephoneNumber()).isNull();
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

        when(ldapRepository.getDeliusUserNoRoles("sandrasmith")).thenReturn(Optional.empty());

        assertThat(staffService.getStaffDetailsByUsername("sandrasmith")).get().extracting(StaffDetails::getEmail).isNull();
    }

    @Test
    public void willReturnCorrectDetailsForMultipleUsernames_getStaffDetailsByUsernames() {
        Set<String> usernames = Set.of("joefrazier", "georgeforeman");

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

        var frazierNDelius = NDeliusUser.builder().telephoneNumber("111 222").mail("joefrazier@service.com").build();
        var foremanNDelius = NDeliusUser.builder().telephoneNumber("333 444").mail("georgeforeman@service.com").build();
        when(ldapRepository.getDeliusUserNoRoles("joefrazier")).thenReturn(Optional.of(frazierNDelius));
        when(ldapRepository.getDeliusUserNoRoles("georgeforeman")).thenReturn(Optional.of(foremanNDelius));

        List<StaffDetails> staffDetailsList = staffService.getStaffDetailsByUsernames(usernames);

        var frazierUserDetails = staffDetailsList.stream().filter(s -> s.getUsername().equals("joefrazier")).findFirst().orElseThrow();
        var foremanUserDetails = staffDetailsList.stream().filter(s -> s.getUsername().equals("georgeforeman")).findFirst().orElseThrow();

        assertThat(staffDetailsList.size()).isEqualTo(2);
        assertThat(frazierUserDetails.getEmail()).isEqualTo("joefrazier@service.com");
        assertThat(foremanUserDetails.getEmail()).isEqualTo("georgeforeman@service.com");
        assertThat(frazierUserDetails.getUsername()).isEqualTo("joefrazier");
        assertThat(foremanUserDetails.getUsername()).isEqualTo("georgeforeman");
    }

    @Test
    public void willReturnStaffIfFoundWithoutCreatingANewOne() {
        when(staffRepository
                .findFirstBySurnameIgnoreCaseAndForenameIgnoreCaseAndProbationArea(any(), any(), any(ProbationArea.class)))
                .thenReturn(Optional.of(aStaff("N01A123456")));

        assertThat(staffService.findOrCreateStaffInArea(ContactableHuman
                .builder()
                .forenames("Sandra")
                .surname("Biggins")
                .build(), aProbationArea()).getOfficerCode()).isEqualTo("N01A123456");

        verify(staffRepository).findFirstBySurnameIgnoreCaseAndForenameIgnoreCaseAndProbationArea(eq("Biggins"), eq("Sandra"), isA(ProbationArea.class));
        verify(staffRepository, never()).save(any());
    }

    @Test
    public void willLookupByFirstForenameWhenSpaceSeparatedWhenSearchingStaffByName() {
        when(staffRepository
                .findFirstBySurnameIgnoreCaseAndForenameIgnoreCaseAndProbationArea(any(), any(), any(ProbationArea.class)))
                .thenReturn(Optional.of(aStaff("N01A123456")));

        assertThat(staffService.findOrCreateStaffInArea(ContactableHuman
                .builder()
                .forenames("Sandra Jane")
                .surname("Biggins")
                .build(), aProbationArea()).getOfficerCode()).isEqualTo("N01A123456");

        verify(staffRepository).findFirstBySurnameIgnoreCaseAndForenameIgnoreCaseAndProbationArea(eq("Biggins"), eq("Sandra"), isA(ProbationArea.class));
        verify(staffRepository, never()).save(any());
    }

    @Test
    public void willLookupByFirstForenameWhenCommaSeparatedWhenSearchingStaffByName() {
        when(staffRepository
                .findFirstBySurnameIgnoreCaseAndForenameIgnoreCaseAndProbationArea(any(), any(), any(ProbationArea.class)))
                .thenReturn(Optional.of(aStaff("N01A123456")));

        assertThat(staffService.findOrCreateStaffInArea(ContactableHuman
                .builder()
                .forenames("Sandra, Jane")
                .surname("Biggins")
                .build(), aProbationArea()).getOfficerCode()).isEqualTo("N01A123456");

        verify(staffRepository).findFirstBySurnameIgnoreCaseAndForenameIgnoreCaseAndProbationArea(eq("Biggins"), eq("Sandra"), isA(ProbationArea.class));
        verify(staffRepository, never()).save(any());
    }

    @Test
    public void willGenerateNewStaffCodeCreateNewStaffWhenNotFoundByName( ) {
        when(staffRepository
                .findFirstBySurnameIgnoreCaseAndForenameIgnoreCaseAndProbationArea(any(), any(), any(ProbationArea.class)))
                .thenReturn(Optional.empty());
        when(staffHelperRepository.getNextStaffCode("N02")).thenReturn("N02A123456");
        when(staffRepository.save(any())).then(params -> params.getArgument(0));

        assertThat(staffService.findOrCreateStaffInArea(
            ContactableHuman
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
                .findFirstBySurnameIgnoreCaseAndForenameIgnoreCaseAndProbationArea(any(), any(), any(ProbationArea.class)))
                .thenReturn(Optional.empty());
        when(staffHelperRepository.getNextStaffCode(any())).thenReturn("N02A123456");

        staffService.findOrCreateStaffInArea(
            ContactableHuman
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
                .findFirstBySurnameIgnoreCaseAndForenameIgnoreCaseAndProbationArea(any(), any(), any(ProbationArea.class)))
                .thenReturn(Optional.empty());
        when(staffHelperRepository.getNextStaffCode(any())).thenReturn("N02A123456");

        staffService.findOrCreateStaffInArea(
            ContactableHuman
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

    @DisplayName("createUnallocatedStaffInArea")
    @Nested
    class CreateUnallocatedStaffInArea {
        @BeforeEach
        void setUp() {

            staffService.createUnallocatedStaffInArea("POM", aProbationArea().toBuilder().code("MDI").build());
            verify(staffRepository).save(staffCaptor.capture());

        }

        @Test
        @DisplayName("Will create staff with unallocated name")
        void willCreateStaffWithUnallocatedName() {
            assertThat(staffCaptor.getValue().getForename()).isEqualTo("Unallocated");
            assertThat(staffCaptor.getValue().getSurname()).isEqualTo("Staff");
        }

        @Test
        @DisplayName("Will create staff with code from prefix and probation area code with U at the end")
        void willCreateStaffWithCodeFromPrefixAndProbationAreaCode() {
            assertThat(staffCaptor.getValue().getOfficerCode()).isEqualTo("MDIPOMU");
        }
    }
    @Test
    public void getManageSupervisionsEligibleOffendersByUsername() {
        when(offenderRepository.getOffendersWithOneActiveEventCommunitySentenceAndRarRequirementForStaff(any(), any())).thenReturn(new PageImpl(
            ImmutableList.of(Offender.builder().crn("X12345").firstName("Brian").secondName("Simon").surname("Friar").build(),
                Offender.builder().crn("X45521").firstName("Tyler").secondName("Argyll").surname("Adams").build()))
        );

        var cases = staffService.getManageSupervisionsEligibleOffendersByUsername("ABC123", Pageable.unpaged());

        assertThat(cases).containsExactly(StaffCaseloadEntry.builder().crn("X12345").firstName("Brian").middleNames(ImmutableList.of("Simon")).surname("Friar").build(),
            StaffCaseloadEntry.builder().crn("X45521").firstName("Tyler").middleNames(ImmutableList.of("Argyll")).surname("Adams").build());
    }

    @Test
    public void getStaffIdByOfficerCode() {
        when(staffRepository.findStaffIdByOfficerCode("ABC1234")).thenReturn(Optional.of(123L));

        var staffId = staffService.getStaffIdByStaffCode("ABC1234");

        assertThat(staffId).isEqualTo(Optional.of(123L));
    }
}
