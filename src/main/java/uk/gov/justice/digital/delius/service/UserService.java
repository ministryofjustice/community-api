package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.AccessLimitation;
import uk.gov.justice.digital.delius.data.api.UserDetails;
import uk.gov.justice.digital.delius.data.api.UserRole;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderAccessLimitations;
import uk.gov.justice.digital.delius.ldap.repository.LdapRepository;
import uk.gov.justice.digital.delius.service.wrapper.UserRepositoryWrapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class UserService {
    private final UserRepositoryWrapper userRepositoryWrapper;
    private final LdapRepository ldapRepository;
    private final TelemetryClient telemetryClient;

    @Autowired
    public UserService(final UserRepositoryWrapper userRepositoryWrapper, final LdapRepository ldapRepository, final TelemetryClient telemetryClient) {
        this.userRepositoryWrapper = userRepositoryWrapper;
        this.ldapRepository = ldapRepository;
        this.telemetryClient = telemetryClient;
    }

    @Transactional(readOnly = true)
    public AccessLimitation accessLimitationOf(final String subject, final OffenderAccessLimitations offenderDetail) {
        final var accessLimitationBuilder = AccessLimitation.builder();

        if (offenderDetail.getCurrentExclusion() || offenderDetail.getCurrentRestriction()) {
            final var user = userRepositoryWrapper.getUser(subject);

            if (offenderDetail.getCurrentExclusion()) {
                final var userExcluded = user.isExcludedFrom(offenderDetail.getOffenderId());
                accessLimitationBuilder.userExcluded(userExcluded);
                if (userExcluded) {
                    accessLimitationBuilder.exclusionMessage(offenderDetail.getExclusionMessage());
                }
            }

            if (offenderDetail.getCurrentRestriction()) {
                final var userRestricted = !user.isRestrictedUserFor(offenderDetail.getOffenderId());
                accessLimitationBuilder.userRestricted(userRestricted);
                if (userRestricted) {
                    accessLimitationBuilder.restrictionMessage(offenderDetail.getRestrictionMessage());
                }
            }
        }

        return accessLimitationBuilder.build();
    }

    public Optional<UserDetails> getUserDetails(final String username) {
        final var ldapUser = ldapRepository.getDeliusUser(username);
        return ldapUser.map(user -> {
            final var oracleUser = userRepositoryWrapper.getUser(username);
            return UserDetails
                        .builder()
                        .roles(user.getRoles().stream().map(role -> UserRole.builder().name(role.getCn()).build()).collect(toList()))
                        .firstName(user.getGivenname())
                        .surname(user.getSn())
                        .email(user.getMail())
                        .enabled(user.isEnabled())
                        .userId(oracleUser.getUserId())
                        .username(username)
                        .build();
        });
    }

    public List<UserDetails> getUserDetailsByEmail(final String email) {
        // first we perform the LDAP search to get a list of matching records for the email address
        final var userList = ldapRepository.getDeliusUserByEmail(email);

        // next we create a `UserDetails` object for each record. this involves looking up
        // the user in the delius oracle db in order to get the user id. users which exist
        // in the LDAP but not in the delius oracle db are not included in the result.
        return userList.stream()
            .map(user -> {
                final uk.gov.justice.digital.delius.jpa.national.entity.User oracleUser;
                final var userCn = user.getCn();

                try {
                    oracleUser = userRepositoryWrapper.getUser(userCn);
                } catch (final NoSuchUserException e) {
                    log.error("no entry found in delius USER table for LDAP user '{}'", userCn);
                    return null;
                }

                return UserDetails.builder()
                    .roles(user.getRoles().stream().map(role -> UserRole.builder().name(role.getCn()).build()).collect(toList()))
                    .firstName(user.getGivenname())
                    .surname(user.getSn())
                    .email(user.getMail())
                    .enabled(user.isEnabled())
                    .userId(oracleUser.getUserId())
                    .username(userCn)
                    .build();
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public boolean changePassword(final String username, final String password) {
        return ldapRepository.changePassword(username, password);
    }

    public void addRole(final String username, final String roleId) {
        final var allRoles = ldapRepository.getAllRoles();
        if (!allRoles.contains(roleId)) {
            throw new BadRequestException(String.format("Could not find role with id: '%s'", roleId));
        }
        try {
            ldapRepository.addRole(username, roleId);
        } catch (final NameNotFoundException e) {
            throw new NotFoundException(String.format("Could not find user with username: '%s'", username));
        }
        telemetryClient.trackEvent("RoleAssigned", Map.of("username", username, "roleId", roleId), null);
    }
}
