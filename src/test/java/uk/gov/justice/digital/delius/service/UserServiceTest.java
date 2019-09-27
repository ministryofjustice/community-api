package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.digital.delius.data.api.AccessLimitation;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.UserDetails;
import uk.gov.justice.digital.delius.jpa.national.entity.Exclusion;
import uk.gov.justice.digital.delius.jpa.national.entity.Restriction;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.ldap.repository.LdapRepository;
import uk.gov.justice.digital.delius.ldap.repository.entity.NDeliusRole;
import uk.gov.justice.digital.delius.ldap.repository.entity.NDeliusUser;
import uk.gov.justice.digital.delius.service.wrapper.UserRepositoryWrapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@Import({UserService.class})
public class UserServiceTest {
    @MockBean
    private UserRepositoryWrapper userRepositoryWrapper;

    @MockBean
    private LdapRepository ldapRepository;

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
        final AccessLimitation accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
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
        final AccessLimitation accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
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
        final AccessLimitation  accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
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
        final AccessLimitation  accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
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
        final AccessLimitation  accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
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
        final AccessLimitation accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
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
        final AccessLimitation accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
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
        final AccessLimitation  accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
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
        final AccessLimitation  accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
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
        final AccessLimitation  accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
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
                                        .description("The role one")
                                        .build()))
                        .build()));

        final Optional<UserDetails> userDetails = userService.getUserDetails("john.bean");

        assertThat(userDetails.orElseThrow(() -> new RuntimeException("user details missing")).getEmail()).isEqualTo("john.bean@justice.gov.uk");
        assertThat(userDetails.orElseThrow(() -> new RuntimeException("user details missing")).getSurname()).isEqualTo("Bean");
        assertThat(userDetails.orElseThrow(() -> new RuntimeException("user details missing")).getFirstName()).isEqualTo("John");
        assertThat(userDetails.orElseThrow(() -> new RuntimeException("user details missing")).getRoles()).hasSize(1);
        assertThat(userDetails.orElseThrow(() -> new RuntimeException("user details missing")).getRoles().get(0).getName()).isEqualTo("ROLE1");
        assertThat(userDetails.orElseThrow(() -> new RuntimeException("user details missing")).getRoles().get(0).getDescription()).isEqualTo("The role one");
    }

    @Test
    public void userDetailsMayBeAbsentFromLdapRepository() {
        when(ldapRepository.getDeliusUser("john.bean")).thenReturn(Optional.empty());

        final Optional<UserDetails> userDetails = userService.getUserDetails("john.bean");

        assertThat(userDetails.isPresent()).isFalse();

    }

}
