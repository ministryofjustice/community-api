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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccessServiceTest {

    private static final String DO_EXCLUDE = "ROLE_EXCLUDE";
    private static final String DO_RESTRICT = "ROLE_RESTRICT";
    private static final String DO_RESTRICT_AND_EXCLUDE = "ROLE_RESTRICT_AND_EXCLUDE";
    private static final String CRN = "CRN";
    private static final String USER_NAME = "su.metal";
    private static final String RESTRICTED_MESSAGE = "Restricted message";
    private static final String EXCLUDED_MESSAGE = "Excluded message";
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
        userAccessService.checkExclusionsAndRestrictions(CRN, Set.of(new SimpleGrantedAuthority("ROLE_WHATEVER")));
        verifyNoInteractions(offenderService, userService, currentUserSupplier);
    }

    @Test
    public void givenRoleIsInExcluded_andUserIsNotExcluded_thenAccessAllowed(){
        final var accessLimitation = new AccessLimitation(false, RESTRICTED_MESSAGE, false, EXCLUDED_MESSAGE);

        when(offenderService.getOffenderByCrn(CRN)).thenReturn(Optional.of(offender));
        when(currentUserSupplier.username()).thenReturn(Optional.of(USER_NAME));
        when(userService.accessLimitationOf(USER_NAME, offender)).thenReturn(accessLimitation);
        userAccessService.checkExclusionsAndRestrictions(CRN, Set.of(new SimpleGrantedAuthority(DO_EXCLUDE)));

        verify(offenderService, atMostOnce()).getOffenderByCrn(CRN);
        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier, offender);
    }

    @Test
    public void givenRoleIsInExcluded_andUserIsExcluded_thenAccessDENIED(){
        final var accessLimitation = new AccessLimitation(false, RESTRICTED_MESSAGE, true, EXCLUDED_MESSAGE);

        when(offenderService.getOffenderByCrn(CRN)).thenReturn(Optional.of(offender));
        when(currentUserSupplier.username()).thenReturn(Optional.of(USER_NAME));
        when(userService.accessLimitationOf(USER_NAME, offender)).thenReturn(accessLimitation);
        assertThatExceptionOfType(AccessDeniedException.class)
            .isThrownBy(() -> userAccessService.checkExclusionsAndRestrictions(CRN, Set.of(new SimpleGrantedAuthority(DO_EXCLUDE))))
            .withMessage(EXCLUDED_MESSAGE);

        verify(offenderService, atMostOnce()).getOffenderByCrn(CRN);
        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier, offender);
    }

    @Test
    public void givenRoleIsInRestricted_andUserIsNotRestricted_thenAccessAllowed(){
        final var accessLimitation = new AccessLimitation(false, RESTRICTED_MESSAGE, false, EXCLUDED_MESSAGE);

        when(offenderService.getOffenderByCrn(CRN)).thenReturn(Optional.of(offender));
        when(currentUserSupplier.username()).thenReturn(Optional.of(USER_NAME));
        when(userService.accessLimitationOf(USER_NAME, offender)).thenReturn(accessLimitation);
        userAccessService.checkExclusionsAndRestrictions(CRN, Set.of(new SimpleGrantedAuthority(DO_RESTRICT)));

        verify(offenderService, atMostOnce()).getOffenderByCrn(CRN);
        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier, offender);
    }

    @Test
    public void givenRoleIsInRestricted_andUserIsRestricted_thenAccessDENIED(){
        final var accessLimitation = new AccessLimitation(true, RESTRICTED_MESSAGE, true, EXCLUDED_MESSAGE);

        when(offenderService.getOffenderByCrn(CRN)).thenReturn(Optional.of(offender));
        when(currentUserSupplier.username()).thenReturn(Optional.of(USER_NAME));
        when(userService.accessLimitationOf(USER_NAME, offender)).thenReturn(accessLimitation);
        assertThatExceptionOfType(AccessDeniedException.class)
            .isThrownBy(() -> userAccessService.checkExclusionsAndRestrictions(CRN, Set.of(new SimpleGrantedAuthority(DO_RESTRICT))))
            .withMessage(RESTRICTED_MESSAGE);

        verify(offenderService, atMostOnce()).getOffenderByCrn(CRN);
        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier, offender);
    }

    @Test
    public void givenRoleIsInRestrictedAndExcluded_andUserIsRestrictedAndExcluded_thenAccessDENIEDAndRestrictionNotChecked(){
        // Note - this test is primarily to document the actual behaviour and is not based on a specific requirement
        final var accessLimitation = new AccessLimitation(true, RESTRICTED_MESSAGE, true, EXCLUDED_MESSAGE);

        when(offenderService.getOffenderByCrn(CRN)).thenReturn(Optional.of(offender));
        when(currentUserSupplier.username()).thenReturn(Optional.of(USER_NAME));
        when(userService.accessLimitationOf(USER_NAME, offender)).thenReturn(accessLimitation);
        assertThatExceptionOfType(AccessDeniedException.class)
            .isThrownBy(() -> userAccessService.checkExclusionsAndRestrictions(CRN, Set.of(new SimpleGrantedAuthority(DO_RESTRICT_AND_EXCLUDE))))
            .withMessage(EXCLUDED_MESSAGE);

        verify(offenderService, atMostOnce()).getOffenderByCrn(CRN);
        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier, offender);
    }

    @Test
    public void givenRoleIsInRestrictedAndExcluded_andUserIsNotExcluded_thenAccessDENIEDAndOneCallToGetOffender(){
        final var accessLimitation = new AccessLimitation(true, RESTRICTED_MESSAGE, false, EXCLUDED_MESSAGE);

        when(offenderService.getOffenderByCrn(CRN)).thenReturn(Optional.of(offender));
        when(currentUserSupplier.username()).thenReturn(Optional.of(USER_NAME));
        when(userService.accessLimitationOf(USER_NAME, offender)).thenReturn(accessLimitation);
        assertThatExceptionOfType(AccessDeniedException.class)
            .isThrownBy(() -> userAccessService.checkExclusionsAndRestrictions(CRN, Set.of(new SimpleGrantedAuthority(DO_RESTRICT_AND_EXCLUDE))))
            .withMessage(RESTRICTED_MESSAGE);

        // TODO: Check JPA caching behaviour to see if this will make two DB calls
        verify(offenderService, times(2)).getOffenderByCrn(CRN);
        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier, offender);
    }
}