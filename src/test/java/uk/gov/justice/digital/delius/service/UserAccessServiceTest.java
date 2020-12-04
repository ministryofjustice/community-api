package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import uk.gov.justice.digital.delius.data.api.AccessLimitation;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.helpers.CurrentUserSupplier;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccessServiceTest {

    private static final String DO_EXCLUDE = "ROLE_EXCLUDE";
    private static final String DO_RESTRICT = "ROLE_RESTRICT";
    private static final String DO_RESTRICT_AND_EXCLUDE = "ROLE_RESTRICT_AND_EXCLUDE";
    private static final String CRN = "CRN";
    private static final String USER_NAME = "su.metal";
    private static final String RESTRICTION_MESSAGE = "Restricted message";
    private static final String EXCLUSION_MESSAGE = "Excluded message";
    private UserAccessService userAccessService;

    @Mock
    private UserService userService;
    @Mock
    private OffenderService offenderService;
    @Mock
    private CurrentUserSupplier currentUserSupplier;
    @Mock
    private OffenderDetail offender;

    @BeforeEach
    public void setUp(){
        userAccessService = new UserAccessService(userService, offenderService, currentUserSupplier,
            Set.of(DO_EXCLUDE, DO_RESTRICT_AND_EXCLUDE), Set.of(DO_RESTRICT, DO_RESTRICT_AND_EXCLUDE));
    }

    @Test
    public void givenRoleIsNotInExcludedOrRestricted_thenAccessAllowed(){
        when(currentUserSupplier.username()).thenReturn(Optional.of(USER_NAME));

        userAccessService.checkExclusionsAndRestrictions(CRN, Set.of(new SimpleGrantedAuthority("ROLE_WHATEVER")));

        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier);
    }

    @Test
    public void givenRoleIsInExcluded_andUserIsNotExcluded_thenAccessAllowed(){
        final var accessLimitation = new AccessLimitation(false, RESTRICTION_MESSAGE, false, EXCLUSION_MESSAGE);

        when(currentUserSupplier.username()).thenReturn(Optional.of(USER_NAME));
        when(offenderService.getOffenderByCrn(CRN)).thenReturn(Optional.of(offender));
        when(userService.accessLimitationOf(USER_NAME, offender)).thenReturn(accessLimitation);

        userAccessService.checkExclusionsAndRestrictions(CRN, Set.of(new SimpleGrantedAuthority(DO_EXCLUDE)));

        verify(offenderService, atMostOnce()).getOffenderByCrn(CRN);
        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier, offender);
    }

    @Test
    public void givenRoleIsInExcluded_andNoUser_thenAccessAllowed(){
        when(currentUserSupplier.username()).thenReturn(Optional.empty());

        userAccessService.checkExclusionsAndRestrictions(CRN, Set.of(new SimpleGrantedAuthority(DO_EXCLUDE)));

        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier, offender);
    }

    @Test
    public void givenRoleIsInExcluded_andUserIsExcluded_thenAccessDENIED(){
        final var accessLimitation = new AccessLimitation(false, RESTRICTION_MESSAGE, true, EXCLUSION_MESSAGE);

        when(currentUserSupplier.username()).thenReturn(Optional.of(USER_NAME));
        when(offenderService.getOffenderByCrn(CRN)).thenReturn(Optional.of(offender));
        when(userService.accessLimitationOf(USER_NAME, offender)).thenReturn(accessLimitation);

        assertThatExceptionOfType(AccessDeniedException.class)
            .isThrownBy(() -> userAccessService.checkExclusionsAndRestrictions(CRN, Set.of(new SimpleGrantedAuthority(DO_EXCLUDE))))
            .withMessage(EXCLUSION_MESSAGE);

        verify(offenderService, atMostOnce()).getOffenderByCrn(CRN);
        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier, offender);
    }

    @Test
    public void givenFirstRoleNotExcluded_andSecondRoleIsExcluded_andUserIsExcluded_thenAccessDENIED(){
        final var accessLimitation = new AccessLimitation(false, RESTRICTION_MESSAGE, true, EXCLUSION_MESSAGE);

        when(currentUserSupplier.username()).thenReturn(Optional.of(USER_NAME));
        when(offenderService.getOffenderByCrn(CRN)).thenReturn(Optional.of(offender));
        when(userService.accessLimitationOf(USER_NAME, offender)).thenReturn(accessLimitation);

        final var authorities = Set.of(
            new SimpleGrantedAuthority(DO_EXCLUDE),
            new SimpleGrantedAuthority("ROLE_WHATEVER")
        );
        assertThatExceptionOfType(AccessDeniedException.class)
            .isThrownBy(() -> userAccessService.checkExclusionsAndRestrictions(CRN, authorities))
            .withMessage(EXCLUSION_MESSAGE);

        verify(offenderService, atMostOnce()).getOffenderByCrn(CRN);
        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier, offender);
    }

    @Test
    public void givenRoleIsInRestricted_andUserIsNotRestricted_thenAccessAllowed(){
        final var accessLimitation = new AccessLimitation(false, RESTRICTION_MESSAGE, false, EXCLUSION_MESSAGE);

        when(currentUserSupplier.username()).thenReturn(Optional.of(USER_NAME));
        when(offenderService.getOffenderByCrn(CRN)).thenReturn(Optional.of(offender));
        when(userService.accessLimitationOf(USER_NAME, offender)).thenReturn(accessLimitation);

        userAccessService.checkExclusionsAndRestrictions(CRN, Set.of(new SimpleGrantedAuthority(DO_RESTRICT)));

        verify(offenderService, atMostOnce()).getOffenderByCrn(CRN);
        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier, offender);
    }

    @Test
    public void givenRoleIsInRestricted_andNoUser_thenAccessDenied(){
        when(currentUserSupplier.username()).thenReturn(Optional.empty());
        when(offenderService.getOffenderByCrn(CRN)).thenReturn(Optional.of(offender));
        when(offender.getCurrentRestriction()).thenReturn(true);
        when(offender.getRestrictionMessage()).thenReturn(RESTRICTION_MESSAGE);
        when(offender.getExclusionMessage()).thenReturn(EXCLUSION_MESSAGE);

        assertThatExceptionOfType(AccessDeniedException.class)
            .isThrownBy(() -> userAccessService.checkExclusionsAndRestrictions(CRN, Set.of(new SimpleGrantedAuthority(DO_RESTRICT))))
            .withMessage(RESTRICTION_MESSAGE);

        verify(offenderService, atMostOnce()).getOffenderByCrn(CRN);
        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier, offender);
    }

    @Test
    public void givenRoleIsInRestricted_andUserIsRestricted_thenAccessDENIED(){
        final var accessLimitation = new AccessLimitation(true, RESTRICTION_MESSAGE, true, EXCLUSION_MESSAGE);

        when(currentUserSupplier.username()).thenReturn(Optional.of(USER_NAME));
        when(offenderService.getOffenderByCrn(CRN)).thenReturn(Optional.of(offender));
        when(userService.accessLimitationOf(USER_NAME, offender)).thenReturn(accessLimitation);

        assertThatExceptionOfType(AccessDeniedException.class)
            .isThrownBy(() -> userAccessService.checkExclusionsAndRestrictions(CRN, Set.of(new SimpleGrantedAuthority(DO_RESTRICT))))
            .withMessage(RESTRICTION_MESSAGE);

        verify(offenderService, atMostOnce()).getOffenderByCrn(CRN);
        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier, offender);
    }

    @Test
    public void givenRoleIsInRestrictedAndExcluded_andUserIsRestrictedAndExcluded_thenAccessDENIEDAndRestrictionNotChecked(){
        final var accessLimitation = new AccessLimitation(true, RESTRICTION_MESSAGE, true, EXCLUSION_MESSAGE);

        when(currentUserSupplier.username()).thenReturn(Optional.of(USER_NAME));
        when(offenderService.getOffenderByCrn(CRN)).thenReturn(Optional.of(offender));
        when(userService.accessLimitationOf(USER_NAME, offender)).thenReturn(accessLimitation);

        assertThatExceptionOfType(AccessDeniedException.class)
            .isThrownBy(() -> userAccessService.checkExclusionsAndRestrictions(CRN, Set.of(new SimpleGrantedAuthority(DO_RESTRICT_AND_EXCLUDE))))
            .withMessage(EXCLUSION_MESSAGE);

        verify(offenderService, atMostOnce()).getOffenderByCrn(CRN);
        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier, offender);
    }

    @Test
    public void givenRoleIsInRestrictedAndExcluded_andUserIsNotExcluded_thenAccessDENIED(){
        final var accessLimitation = new AccessLimitation(true, RESTRICTION_MESSAGE, false, EXCLUSION_MESSAGE);

        when(currentUserSupplier.username()).thenReturn(Optional.of(USER_NAME));
        when(offenderService.getOffenderByCrn(CRN)).thenReturn(Optional.of(offender));
        when(userService.accessLimitationOf(USER_NAME, offender)).thenReturn(accessLimitation);

        assertThatExceptionOfType(AccessDeniedException.class)
            .isThrownBy(() -> userAccessService.checkExclusionsAndRestrictions(CRN, Set.of(new SimpleGrantedAuthority(DO_RESTRICT_AND_EXCLUDE))))
            .withMessage(RESTRICTION_MESSAGE);

        verify(offenderService, times(2)).getOffenderByCrn(CRN);
        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier, offender);
    }

    @Test
    public void givenFirstRoleNotRestricted_andSecondRoleRestricted_andUserIsRestricted_thenAccessDENIED(){
        final var accessLimitation = new AccessLimitation(true, RESTRICTION_MESSAGE, false, EXCLUSION_MESSAGE);

        when(currentUserSupplier.username()).thenReturn(Optional.of(USER_NAME));
        when(offenderService.getOffenderByCrn(CRN)).thenReturn(Optional.of(offender));
        when(userService.accessLimitationOf(USER_NAME, offender)).thenReturn(accessLimitation);

        final var authorities = Set.of(
            new SimpleGrantedAuthority(DO_RESTRICT_AND_EXCLUDE),
            new SimpleGrantedAuthority("ROLE_WHATEVER")
        );

        assertThatExceptionOfType(AccessDeniedException.class)
            .isThrownBy(() -> userAccessService.checkExclusionsAndRestrictions(CRN, authorities))
            .withMessage(RESTRICTION_MESSAGE);

        verify(offenderService, times(2)).getOffenderByCrn(CRN);
        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier, offender);
    }
}