package uk.gov.justice.digital.delius.service;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.data.api.AccessLimitation;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.UserDetails;
import uk.gov.justice.digital.delius.data.api.UserRole;
import uk.gov.justice.digital.delius.jpa.national.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.ldap.repository.LdapRepository;
import uk.gov.justice.digital.delius.service.wrapper.UserRepositoryWrapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class UserService {
    private final UserRepositoryWrapper userRepositoryWrapper;
    private final LdapRepository ldapRepository;


    @Autowired
    public UserService(UserRepositoryWrapper userRepositoryWrapper, LdapRepository ldapRepository) {
        this.userRepositoryWrapper = userRepositoryWrapper;
        this.ldapRepository = ldapRepository;
    }


    @Transactional(readOnly = true)
    public AccessLimitation accessLimitationOf(String subject, OffenderDetail offenderDetail) {
        AccessLimitation.AccessLimitationBuilder accessLimitationBuilder = AccessLimitation.builder();

        if (offenderDetail.getCurrentExclusion() || offenderDetail.getCurrentRestriction()) {
            User user = userRepositoryWrapper.getUser(subject);

            if (offenderDetail.getCurrentExclusion()) {
                val userExcluded = user.isExcludedFrom(offenderDetail.getOffenderId());
                accessLimitationBuilder.userExcluded(userExcluded);
                if (userExcluded) {
                    accessLimitationBuilder.exclusionMessage(offenderDetail.getExclusionMessage());
                }
            }

            if (offenderDetail.getCurrentRestriction()) {
                val userRestricted = !user.isRestrictedUserFor(offenderDetail.getOffenderId());
                accessLimitationBuilder.userRestricted(userRestricted);
                if (userRestricted) {
                    accessLimitationBuilder.restrictionMessage(offenderDetail.getRestrictionMessage());
                }
            }
        }

        return accessLimitationBuilder.build();
    }

    @Transactional
    public List<uk.gov.justice.digital.delius.data.api.User> getUsersList(String surname, Optional<String> forename) {

        List<uk.gov.justice.digital.delius.data.api.User> users = forename.map(f -> userRepositoryWrapper.findBySurnameIgnoreCaseAndForenameIgnoreCase(surname, f))
                .orElse(userRepositoryWrapper.findBySurnameIgnoreCase(surname))
                .stream()
                .map(user -> uk.gov.justice.digital.delius.data.api.User.builder()
                        .userId(user.getUserId())
                        .distinguishedName(user.getDistinguishedName())
                        .endDate(user.getEndDate())
                        .externalProviderEmployeeFlag(user.getExternalProviderEmployeeFlag())
                        .externalProviderId(user.getExternalProviderId())
                        .forename(user.getForename())
                        .forename2(user.getForename2())
                        .surname(user.getSurname())
                        .organisationId(user.getOrganisationId())
                        .privateFlag(user.getPrivateFlag())
                        .scProviderId(user.getScProviderId())
                        .staffId(user.getStaffId())
                        .probationAreaCodes(probationAreaCodesOf(user.getProbationAreas()))
                        .build()).collect(toList());
        return users;
    }

    private List<String> probationAreaCodesOf(List<ProbationArea> probationAreas) {
        return Optional.ofNullable(probationAreas).map(
                pas -> pas.stream().map(ProbationArea::getCode).collect(toList())).orElse(Collections.emptyList());
    }

    public Optional<UserDetails> getUserDetails(String username) {
        return ldapRepository.getDeliusUser(username).map(user ->
                UserDetails
                        .builder()
                        .roles(user.getRoles().stream().map(role -> UserRole.builder().name(role.getCn()).description(role.getDescription()).build()).collect(toList()))
                        .firstName(user.getGivenname())
                        .surname(user.getSn())
                        .email(user.getMail())
                        .locked(user.getOrclActiveEndDate() != null) // TODO check date not the presence of the date
                        .build());
    }

    public boolean changePassword(String username, String password) {
        return ldapRepository.changePassword(username, password);
    }

    public boolean lockAccount(String username) {
        return ldapRepository.lockAccount(username);
    }

    public boolean unlockAccount(String username) {
        return ldapRepository.unlockAccount(username);
    }
}
