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

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccessServiceTest {

    private static final String SCOPE_IGNORE_EXCLUSIONS = "SCOPE_IGNORE_EXCLUSIONS_ALWAYS";
    private static final String SCOPE_IGNORE_RESTRICTIONS = "SCOPE_IGNORE_INCLUSIONS_ALWAYS";
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
            Set.of(SCOPE_IGNORE_EXCLUSIONS), Set.of(SCOPE_IGNORE_RESTRICTIONS));
    }

    @Test
    public void givenUserIsNotExcludedOrRestricted_thenAccessAllowed(){
        final var accessLimitation = new AccessLimitation(false, RESTRICTION_MESSAGE, false, EXCLUSION_MESSAGE);

        when(currentUserSupplier.username()).thenReturn(Optional.of(USER_NAME));
        when(offenderService.getOffenderByCrn(CRN)).thenReturn(Optional.of(offender));
        when(userService.accessLimitationOf(USER_NAME, offender)).thenReturn(accessLimitation);

        userAccessService.checkExclusionsAndRestrictions(CRN, Collections.emptySet());

        verify(offenderService, atMostOnce()).getOffenderByCrn(CRN);
        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier, offender);
    }

    @Test
    public void givenUserIsExcluded_thenAccessDENIED(){
        final var accessLimitation = new AccessLimitation(false, RESTRICTION_MESSAGE, true, EXCLUSION_MESSAGE);

        when(currentUserSupplier.username()).thenReturn(Optional.of(USER_NAME));
        when(offenderService.getOffenderByCrn(CRN)).thenReturn(Optional.of(offender));
        when(userService.accessLimitationOf(USER_NAME, offender)).thenReturn(accessLimitation);

        assertThatExceptionOfType(AccessDeniedException.class)
            .isThrownBy(() -> userAccessService.checkExclusionsAndRestrictions(CRN, Collections.emptySet()))
            .withMessage(EXCLUSION_MESSAGE);

        verify(offenderService, atMostOnce()).getOffenderByCrn(CRN);
        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier, offender);
    }

    @Test
    public void givenUserIsExcluded_andScopeIsIgnoreExclusions_thenAccessAllowed(){
        final var accessLimitation = new AccessLimitation(false, RESTRICTION_MESSAGE, true, EXCLUSION_MESSAGE);

        when(currentUserSupplier.username()).thenReturn(Optional.of(USER_NAME));
        when(offenderService.getOffenderByCrn(CRN)).thenReturn(Optional.of(offender));
        when(userService.accessLimitationOf(USER_NAME, offender)).thenReturn(accessLimitation);

        userAccessService.checkExclusionsAndRestrictions(CRN, Set.of(new SimpleGrantedAuthority(SCOPE_IGNORE_EXCLUSIONS)));

        verify(offenderService, atMostOnce()).getOffenderByCrn(CRN);
        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier, offender);
    }

    @Test
    public void givenOffenderIsRestricted_andNoUser_thenAccessDenied(){
        when(currentUserSupplier.username()).thenReturn(Optional.empty());
        when(offenderService.getOffenderByCrn(CRN)).thenReturn(Optional.of(offender));
        when(offender.getCurrentRestriction()).thenReturn(true);
        when(offender.getRestrictionMessage()).thenReturn(RESTRICTION_MESSAGE);
        when(offender.getExclusionMessage()).thenReturn(EXCLUSION_MESSAGE);

        assertThatExceptionOfType(AccessDeniedException.class)
            .isThrownBy(() -> userAccessService.checkExclusionsAndRestrictions(CRN, Collections.emptySet()))
            .withMessage(RESTRICTION_MESSAGE);

        verify(offenderService, atMostOnce()).getOffenderByCrn(CRN);
        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier, offender);
    }

    @Test
    public void givenUserIsRestricted_thenAccessDENIED(){
        final var accessLimitation = new AccessLimitation(true, RESTRICTION_MESSAGE, false, EXCLUSION_MESSAGE);

        when(currentUserSupplier.username()).thenReturn(Optional.of(USER_NAME));
        when(offenderService.getOffenderByCrn(CRN)).thenReturn(Optional.of(offender));
        when(userService.accessLimitationOf(USER_NAME, offender)).thenReturn(accessLimitation);

        assertThatExceptionOfType(AccessDeniedException.class)
            .isThrownBy(() -> userAccessService.checkExclusionsAndRestrictions(CRN, Collections.emptySet()))
            .withMessage(RESTRICTION_MESSAGE);

        verify(offenderService, atMostOnce()).getOffenderByCrn(CRN);
        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier, offender);
    }

    @Test
    public void givenUserIsRestricted_andScopeIsIgnoreRestrictions_thenAccessAllowed(){
        final var accessLimitation = new AccessLimitation(true, RESTRICTION_MESSAGE, false, EXCLUSION_MESSAGE);

        when(currentUserSupplier.username()).thenReturn(Optional.of(USER_NAME));
        when(offenderService.getOffenderByCrn(CRN)).thenReturn(Optional.of(offender));
        when(userService.accessLimitationOf(USER_NAME, offender)).thenReturn(accessLimitation);

        userAccessService.checkExclusionsAndRestrictions(CRN, Set.of(new SimpleGrantedAuthority(SCOPE_IGNORE_RESTRICTIONS)));

        verify(offenderService, atMostOnce()).getOffenderByCrn(CRN);
        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier, offender);
    }

    @Test
    public void givenUserIsRestrictedAndExcluded_thenAccessDENIEDAndRestrictionNotChecked(){
        final var accessLimitation = new AccessLimitation(true, RESTRICTION_MESSAGE, true, EXCLUSION_MESSAGE);

        when(currentUserSupplier.username()).thenReturn(Optional.of(USER_NAME));
        when(offenderService.getOffenderByCrn(CRN)).thenReturn(Optional.of(offender));
        when(userService.accessLimitationOf(USER_NAME, offender)).thenReturn(accessLimitation);

        assertThatExceptionOfType(AccessDeniedException.class)
            .isThrownBy(() -> userAccessService.checkExclusionsAndRestrictions(CRN, Collections.emptySet()))
            .withMessage(EXCLUSION_MESSAGE);

        verify(offenderService, atMostOnce()).getOffenderByCrn(CRN);
        verifyNoMoreInteractions(offenderService, userService, currentUserSupplier, offender);
    }
}