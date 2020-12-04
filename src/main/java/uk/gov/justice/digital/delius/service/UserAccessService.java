package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.AccessLimitation;
import uk.gov.justice.digital.delius.helpers.CurrentUserSupplier;

import java.util.Collection;
import java.util.Set;

@Service
@AllArgsConstructor
public class UserAccessService {
    private final UserService userService;
    private final OffenderService offenderService;
    private final CurrentUserSupplier currentUserSupplier;
    @Value("${user-access.roles.apply-exclusions-for}")
    private final Set<String> excludingRoles;
    @Value("${user-access.roles.apply-restrictions-for}")
    private final Set<String> restrictingRoles;

    public void checkExclusionsAndRestrictions(String crn, Collection<? extends GrantedAuthority> authorities) {
        final var username = currentUserSupplier.username();
        if (username.isPresent() && shouldCheckExclusion(authorities)) {

            final var excludedException = offenderService.getOffenderByCrn(crn)
                .map(offender -> userService.accessLimitationOf(username.get(), offender))
                .filter(AccessLimitation::isUserExcluded)
                .map(accessLimitation -> new AccessDeniedException(accessLimitation.getExclusionMessage()));

            if (excludedException.isPresent())
                throw excludedException.get();
        }

        if (shouldCheckRestriction(authorities)) {
            final var restrictedException = offenderService.getOffenderByCrn(crn)
                .map(offender -> username.map(u -> userService.accessLimitationOf(u, offender))
                                         .orElseGet(() -> buildAnonymousUserAccessLimitation(offender)))
                .filter(AccessLimitation::isUserRestricted)
                .map(accessLimitation -> new AccessDeniedException(accessLimitation.getRestrictionMessage()));

            if (restrictedException.isPresent())
                throw restrictedException.get();
        }
    }

    private boolean shouldCheckExclusion(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .map(String::toUpperCase)
            .anyMatch(excludingRoles::contains);
    }

    private boolean shouldCheckRestriction(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .map(String::toUpperCase)
            .anyMatch(restrictingRoles::contains);
    }

    private AccessLimitation buildAnonymousUserAccessLimitation(uk.gov.justice.digital.delius.data.api.OffenderDetail offender) {
        return AccessLimitation.builder()
            .userRestricted(offender.getCurrentRestriction())
            .restrictionMessage(offender.getRestrictionMessage())
            .userExcluded(false)    // Can't exclude without a username
            .exclusionMessage(offender.getExclusionMessage())
            .build();
    }
}