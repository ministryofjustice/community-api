package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.data.api.AccessLimitation;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.service.wrapper.UserRepositoryWrapper;

@Service
public class UserService {
    private final UserRepositoryWrapper userRepositoryWrapper;

    @Autowired
    public UserService(UserRepositoryWrapper userRepositoryWrapper) {
        this.userRepositoryWrapper = userRepositoryWrapper;
    }


    @Transactional(readOnly = true)
    public AccessLimitation accessLimitationOf(String subject, OffenderDetail offenderDetail) {
        AccessLimitation.AccessLimitationBuilder accessLimitationBuilder = AccessLimitation.builder();

        if (offenderDetail.getCurrentExclusion() || offenderDetail.getCurrentRestriction()) {
            User user = userRepositoryWrapper.getUser(subject);

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
