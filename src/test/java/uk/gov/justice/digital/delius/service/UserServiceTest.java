package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.jpa.national.entity.Exclusion;
import uk.gov.justice.digital.delius.jpa.national.entity.Restriction;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.service.wrapper.UserRepositoryWrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@Import({UserService.class})
public class UserServiceTest {
    @MockBean
    private UserRepositoryWrapper userRepositoryWrapper;
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
        val accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
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
        val accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
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
       val  accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
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
       val  accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
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
       val  accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
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
        val accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
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
        val accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
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
        val  accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
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
        val  accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
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
        val  accessLimitation = userService.accessLimitationOf("Micky", OffenderDetail
                .builder()
                .currentExclusion(false)
                .currentRestriction(true)
                .exclusionMessage("How dare you - you are excluded")
                .restrictionMessage("How dare you - you are restricted")
                .offenderId(1L)
                .build());

        assertThat(accessLimitation.getRestrictionMessage()).isEqualTo("How dare you - you are restricted");
    }


}