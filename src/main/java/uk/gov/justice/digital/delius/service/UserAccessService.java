package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.controller.secure.OffendersResource;
import uk.gov.justice.digital.delius.data.api.AccessLimitation;
import uk.gov.justice.digital.delius.helpers.CurrentUserSupplier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;

@Service
@AllArgsConstructor
public class UserAccessService {
    private final UserService userService;
    private final OffenderService offenderService;
    private final CurrentUserSupplier currentUserSupplier;

    public void checkExclusionsAndRestrictions(String crn, Collection<? extends GrantedAuthority> authorities) {
        final var excludedRoles = Set.of("ROLE_COMMUNITY_API_EXCLUDED", "ROLE_COMMUNITY_API_EXCLUDED_RESTRICTED");
        if (authorities.stream().anyMatch(grantedAuthority -> excludedRoles.contains(grantedAuthority.getAuthority().toUpperCase()))) {

            final var excluded = offenderService.getOffenderByCrn(crn)
                .map(offender -> userService.accessLimitationOf(currentUserSupplier.username().orElseThrow(), offender))
                .filter(AccessLimitation::isUserExcluded)
                .map(accessLimitation -> new AccessDeniedException(accessLimitation.getExclusionMessage()));

            if (excluded.isPresent())
                throw excluded.get();
        }
    }
}