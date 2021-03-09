package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepositoryWrapper userRepositoryWrapper;

    @Mock
    private LdapRepository ldapRepository;

    @Mock
    private TelemetryClient telemetryClient;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepositoryWrapper, ldapRepository, telemetryClient);
    }

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
                        .username("john.bean")
                        .build());
    }

    @Test
    public void userDetailsMayBeAbsentFromLdapRepository() {
        when(ldapRepository.getDeliusUser("john.bean")).thenReturn(Optional.empty());

        final var userDetails = userService.getUserDetails("john.bean");

        assertThat(userDetails.isPresent()).isFalse();
    }

    @Test public void userDetailsByEmailMappedFromLdapRepository() {
        when(ldapRepository.getDeliusUserByEmail(anyString())).thenReturn(
            List.of(
                NDeliusUser.builder()
                    .givenname("John")
                    .sn("Bean")
                    .cn("john.bean")
                    .mail("john.bean@justice.gov.uk")
                    .roles(ImmutableList.of(NDeliusRole.builder().cn("ROLE1").build()))
                    .build())
        );
        when(userRepositoryWrapper.getUser(any())).thenReturn(User.builder().userId(12345L).build());

        final var userDetails = userService.getUserDetailsByEmail("john.bean@justice.gov.uk");

        assertThat(userDetails.size()).isEqualTo(1);
        assertThat(userDetails.get(0)).isEqualTo(
            UserDetails.builder()
                .email("john.bean@justice.gov.uk")
                .surname("Bean")
                .firstName("John")
                .roles(List.of(UserRole.builder().name("ROLE1").build()))
                .enabled(true)
                .userId(12345L)
                .username("john.bean")
                .build());
    }

    @Test public void userDetailsByEmailHandlesMissingDataInDeliusDb() {
        // when the ldap search finds two users, but only one of them is present
        // in the oracle db, only one should be returned in the final result.
        when(ldapRepository.getDeliusUserByEmail(anyString())).thenReturn(
            List.of(
                NDeliusUser.builder()
                    .givenname("John")
                    .sn("Bean")
                    .cn("john.bean")
                    .mail("john.bean@justice.gov.uk")
                    .roles(ImmutableList.of(NDeliusRole.builder().cn("ROLE1").build()))
                    .build(),
                NDeliusUser.builder()
                    .givenname("Al")
                    .sn("Green")
                    .cn("al.green")
                    .mail("al.green@justice.gov.uk")
                    .roles(ImmutableList.of(NDeliusRole.builder().cn("ROLE2").build()))
                    .build())
        );
        when(userRepositoryWrapper.getUser("al.green")).thenThrow(new NoSuchUserException(""));
        when(userRepositoryWrapper.getUser("john.bean")).thenReturn(User.builder().userId(12345L).build());

        final var userDetails = userService.getUserDetailsByEmail("john.bean@justice.gov.uk");

        assertThat(userDetails.size()).isEqualTo(1);
        assertThat(userDetails.get(0)).isEqualTo(
                UserDetails.builder()
                    .email("john.bean@justice.gov.uk")
                    .surname("Bean")
                    .firstName("John")
                    .roles(List.of(UserRole.builder().name("ROLE1").build()))
                    .enabled(true)
                    .userId(12345L)
                    .username("john.bean")
                    .build());
    }

    @Test public void userDetailsByEmailMyBeEmptyListFromLdapRepository() {
        when(ldapRepository.getDeliusUserByEmail(anyString())).thenReturn(Collections.emptyList());

        final var userDetails = userService.getUserDetailsByEmail("john.bean@justice.gov.uk");

        assertThat(userDetails).isEmpty();
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
}
