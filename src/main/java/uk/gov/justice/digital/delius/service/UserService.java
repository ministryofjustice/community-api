package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.AccessLimitation;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.jpa.entity.User;
import uk.gov.justice.digital.delius.jpa.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private User getUser(String userDistinguishedName) {
        return userRepository.findByDistinguishedName(userDistinguishedName).orElseThrow(() -> new NoSuchUserException("Can't resolve user: " + userDistinguishedName));
    }

    public AccessLimitation accessLimitationOf(String subject, OffenderDetail offenderDetail) {
        AccessLimitation.AccessLimitationBuilder accessLimitationBuilder = AccessLimitation.builder();

        if (offenderDetail.getCurrentExclusion() || offenderDetail.getCurrentRestriction()) {
            User user = getUser(subject);

            if (offenderDetail.getCurrentExclusion()) {
                accessLimitationBuilder.userExcluded(user.isExcludedFrom(offenderDetail.getOffenderId()));
            }

            if (offenderDetail.getCurrentRestriction()) {
                accessLimitationBuilder.userRestricted(!user.isRestrictedUserFor(offenderDetail.getOffenderId()));
            }
        }

        return accessLimitationBuilder.build();
    }
}
