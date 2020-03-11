package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.UserDetails;
import uk.gov.justice.digital.delius.data.api.UserRole;
import uk.gov.justice.digital.delius.jpa.national.entity.Exclusion;
import uk.gov.justice.digital.delius.jpa.national.entity.Restriction;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.ldap.repository.LdapRepository;
import uk.gov.justice.digital.delius.ldap.repository.entity.NDeliusRole;
import uk.gov.justice.digital.delius.ldap.repository.entity.NDeliusUser;
import uk.gov.justice.digital.delius.service.wrapper.UserRepositoryWrapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@Import({UserService.class})
public class UserServiceTest {
    @MockBean
    private UserRepositoryWrapper userRepositoryWrapper;

    @MockBean
    private LdapRepository ldapRepository;

    @MockBean
    private TelemetryClient telemetryClient;

    @Autowired
    private UserService userService;

    @Test
    public void exclusionListNotCheckedWhenNotExcludedForAnyone() {
        userService.accessLimitationOf("Micky", OffenderDetail
                .builder()
                .currentExclusion(false)
                .currentRestriction(false)
                .exclusionMessage("How dare you - you are excluded")
                .restrictionMessage("How dare you - you are restricted")
                .build());

        verify(userRepositoryWrapper, never()).getUser(Mockito.any());
    }


    @Test
    public void exclusionUnsetWhenNotExcludedForAnyone() {
        final var accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
                .builder()
                .currentExclusion(false)
                .currentRestriction(false)
                .exclusionMessage("How dare you - you are excluded")
                .restrictionMessage("How dare you - you are restricted")
                .build());

        assertThat(accessLimitation.isUserExcluded()).isFalse();
    }

    @Test
    public void excludedMessageUnsetWhenNotExcludedForAnyone() {
        final var accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
                .builder()
                .currentExclusion(false)
                .currentRestriction(false)
                .exclusionMessage("How dare you - you are excluded")
                .restrictionMessage("How dare you - you are restricted")
                .build());

        assertThat(accessLimitation.getExclusionMessage()).isNull();
    }

    @Test
    public void exclusionListCheckedWhenExcludedForSomeone() {
        when(userRepositoryWrapper.getUser(any())).thenReturn(User
                .builder()
                .exclusions(ImmutableList.of(Exclusion
                        .builder()
                        .offenderId(2L)
                        .build()))
                .build());
        userService.accessLimitationOf("Micky", OffenderDetail
                .builder()
                .currentExclusion(true)
                .currentRestriction(false)
                .exclusionMessage("How dare you - you are excluded")
                .restrictionMessage("How dare you - you are restricted")
                .offenderId(1L)
                .build());

        verify(userRepositoryWrapper).getUser("Micky");
    }

    @Test
    public void exclusionUnsetWhenUserNotInExclusionList() {
        when(userRepositoryWrapper.getUser(any())).thenReturn(User
                .builder()
                .exclusions(ImmutableList.of(Exclusion
                        .builder()
                        .offenderId(2L)
                        .build()))
                .build());
        final var accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
                .builder()
                .currentExclusion(true)
                .currentRestriction(false)
                .exclusionMessage("How dare you - you are excluded")
                .restrictionMessage("How dare you - you are restricted")
                .offenderId(1L)
                .build());

        assertThat(accessLimitation.isUserExcluded()).isFalse();
    }

    @Test
    public void exclusionSetWhenUserIsInExclusionList() {
        when(userRepositoryWrapper.getUser(any())).thenReturn(User
                .builder()
                .exclusions(ImmutableList.of(Exclusion
                        .builder()
                        .offenderId(1L)
                        .build()))
                .build());
        final var accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
                .builder()
                .currentExclusion(true)
                .currentRestriction(false)
                .exclusionMessage("How dare you - you are excluded")
                .restrictionMessage("How dare you - you are restricted")
                .offenderId(1L)
                .build());

        assertThat(accessLimitation.isUserExcluded()).isTrue();
    }

    @Test
    public void exclusionMessageReturnedWhenUserIsInExclusionList() {
        when(userRepositoryWrapper.getUser(any())).thenReturn(User
                .builder()
                .exclusions(ImmutableList.of(Exclusion
                        .builder()
                        .offenderId(1L)
                        .build()))
                .build());
        final var accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
                .builder()
                .currentExclusion(true)
                .currentRestriction(false)
                .exclusionMessage("How dare you - you are excluded")
                .restrictionMessage("How dare you - you are restricted")
                .offenderId(1L)
                .build());

        assertThat(accessLimitation.getExclusionMessage()).isEqualTo("How dare you - you are excluded");
    }


    @Test
    public void restrictionListNotCheckedWhenNotRestrictedToAnyone() {
        userService.accessLimitationOf("Micky", OffenderDetail
                .builder()
                .currentExclusion(false)
                .currentRestriction(false)
                .exclusionMessage("How dare you - you are excluded")
                .restrictionMessage("How dare you - you are restricted")
                .build());

        verify(userRepositoryWrapper, never()).getUser(Mockito.any());
    }


    @Test
    public void restrictionUnsetWhenNotRestrictedToAnyone() {
        final var accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
                .builder()
                .currentExclusion(false)
                .currentRestriction(false)
                .exclusionMessage("How dare you - you are excluded")
                .restrictionMessage("How dare you - you are restricted")
                .build());

        assertThat(accessLimitation.isUserRestricted()).isFalse();
    }

    @Test
    public void restrictedMessageUnsetWhenNotRestrictedToAnyone() {
        final var accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
                .builder()
                .currentExclusion(false)
                .currentRestriction(false)
                .exclusionMessage("How dare you - you are excluded")
                .restrictionMessage("How dare you - you are restricted")
                .build());

        assertThat(accessLimitation.getRestrictionMessage()).isNull();
    }

    @Test
    public void restrictedListCheckedWhenRestrictedToSomeone() {
        when(userRepositoryWrapper.getUser(any())).thenReturn(User
                .builder()
                .restrictions(ImmutableList.of(Restriction
                        .builder()
                        .offenderId(1L)
                        .build()))
                .build());
        userService.accessLimitationOf("Micky", OffenderDetail
                .builder()
                .currentExclusion(false)
                .currentRestriction(true)
                .exclusionMessage("How dare you - you are excluded")
                .restrictionMessage("How dare you - you are restricted")
                .offenderId(1L)
                .build());

        verify(userRepositoryWrapper).getUser("Micky");
    }

    @Test
    public void restrictedUnsetWhenUserInRestrictionList() {
        when(userRepositoryWrapper.getUser(any())).thenReturn(User
                .builder()
                .restrictions(ImmutableList.of(Restriction
                        .builder()
                        .offenderId(1L)
                        .build()))
                .build());
        final var accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
                .builder()
                .currentExclusion(false)
                .currentRestriction(true)
                .exclusionMessage("How dare you - you are excluded")
                .restrictionMessage("How dare you - you are restricted")
                .offenderId(1L)
                .build());

        assertThat(accessLimitation.isUserRestricted()).isFalse();
    }

    @Test
    public void restrictedSetWhenUserIsNotInRestrictionList() {
        when(userRepositoryWrapper.getUser(any())).thenReturn(User
                .builder()
                .restrictions(ImmutableList.of(Restriction
                        .builder()
                        .offenderId(2L)
                        .build()))
                .build());
        final var accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
                .builder()
                .currentExclusion(false)
                .currentRestriction(true)
                .exclusionMessage("How dare you - you are excluded")
                .restrictionMessage("How dare you - you are restricted")
                .offenderId(1L)
                .build());

        assertThat(accessLimitation.isUserRestricted()).isTrue();
    }

    @Test
    public void restrictedMessageReturnedWhenUserIsNotInRestrictionList() {
        when(userRepositoryWrapper.getUser(any())).thenReturn(User
                .builder()
                .restrictions(ImmutableList.of(Restriction
                        .builder()
                        .offenderId(2L)
                        .build()))
                .build());
        final var accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
                .builder()
                .currentExclusion(false)
                .currentRestriction(true)
                .exclusionMessage("How dare you - you are excluded")
                .restrictionMessage("How dare you - you are restricted")
                .offenderId(1L)
                .build());

        assertThat(accessLimitation.getRestrictionMessage()).isEqualTo("How dare you - you are restricted");
    }

    @Test
    public void userDetailsMappedFromLdapRepository() {
        when(ldapRepository.getDeliusUser(anyString())).thenReturn(
                Optional.of(NDeliusUser
                        .builder()
                        .givenname("John")
                        .sn("Bean")
                        .mail("john.bean@justice.gov.uk")
                        .roles(ImmutableList.of(
                                NDeliusRole
                                        .builder()
                                        .cn("ROLE1")
                                        .build()))
                        .build()));
        when(userRepositoryWrapper.getUser(any())).thenReturn(User.builder().userId(12345L).build());

        final var userDetails = userService.getUserDetails("john.bean");

        assertThat(userDetails).get().isEqualTo(
                UserDetails.builder()
                        .email("john.bean@justice.gov.uk")
                        .surname("Bean")
                        .firstName("John")
                        .roles(List.of(UserRole.builder().name("ROLE1").build()))
                        .enabled(true)
                        .userId(12345L)
                        .build());
    }

    @Test
    public void userDetailsMayBeAbsentFromLdapRepository() {
        when(ldapRepository.getDeliusUser("john.bean")).thenReturn(Optional.empty());

        final var userDetails = userService.getUserDetails("john.bean");

        assertThat(userDetails.isPresent()).isFalse();
    }

    @Test
    public void canAddRoleToUser() {
        when(ldapRepository.getAllRoles()).thenReturn(List.of("CWBT001", "UWBT060"));

        userService.addRole("john.bean", "UWBT060");

        verify(ldapRepository).addRole("john.bean", "UWBT060");
        verify(telemetryClient).trackEvent("RoleAssigned", Map.of("username", "john.bean", "roleId", "UWBT060"), null);

    }

    @Test
    public void cannotAddRoleThatDoesNotExistToUser() {
        when(ldapRepository.getAllRoles()).thenReturn(List.of("CWBT001"));

        assertThatThrownBy(() -> userService.addRole("john.bean", "UWBT060"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Could not find role with id: 'UWBT060'");

        verify(ldapRepository, never()).addRole(any(), any());
        verifyNoInteractions(telemetryClient);
    }

    @Test
    public void userDetailsListIsCorrectForMultipleUsernames() {
        when(ldapRepository.getDeliusUser("john.bean")).thenReturn(
                Optional.of(NDeliusUser
                        .builder()
                        .givenname("John")
                        .sn("Bean")
                        .mail("john.bean@justice.gov.uk")
                        .roles(ImmutableList.of(
                                NDeliusRole
                                        .builder()
                                        .cn("ROLE1")
                                        .build()))
                        .build()));
        when(ldapRepository.getDeliusUser("rocky.balboa")).thenReturn(
                Optional.of(NDeliusUser
                        .builder()
                        .givenname("Rocky")
                        .sn("Balboa")
                        .mail("rocky.balboa@justice.gov.uk")
                        .roles(ImmutableList.of(
                                NDeliusRole
                                        .builder()
                                        .cn("ROLE1")
                                        .build()))
                        .build()));

        final var userDetails = userService.getUserDetailsList(Set.of("john.bean", "rocky.balboa")).getUserDetailsList();

        assertThat(userDetails.stream().anyMatch( userDetail -> userDetail.getUsername().equals("john.bean"))).isTrue();
        assertThat(userDetails.stream().anyMatch( userDetail -> userDetail.getUsername().equals("rocky.balboa"))).isTrue();

        assertThat(userDetails.stream()
                .filter(u -> u.getUsername().equals("john.bean"))
                .findFirst().orElseThrow()).isEqualTo(
                    UserDetails.builder()
                        .email("john.bean@justice.gov.uk")
                        .firstName("John")
                        .surname("Bean")
                        .enabled(true)
                        .username("john.bean")
                        .build());

        assertThat(userDetails.stream()
                .filter(u -> u.getUsername().equals("rocky.balboa"))
                .findFirst().orElseThrow()).isEqualTo(
                    UserDetails.builder()
                        .email("rocky.balboa@justice.gov.uk")
                        .firstName("Rocky")
                        .surname("Balboa")
                        .enabled(true)
                        .username("rocky.balboa")
                        .build());
    }
}
