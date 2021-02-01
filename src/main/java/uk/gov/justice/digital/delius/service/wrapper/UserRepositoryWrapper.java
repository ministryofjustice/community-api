package uk.gov.justice.digital.delius.service.wrapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.jpa.national.repository.UserRepository;
import uk.gov.justice.digital.delius.jpa.oracle.annotations.NationalUserOverride;
import uk.gov.justice.digital.delius.service.NoSuchUserException;

import java.util.List;

@Component
@Slf4j
public class UserRepositoryWrapper {

    private final UserRepository userRepository;

    @Autowired
    public UserRepositoryWrapper(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @NationalUserOverride
    public User getUser(final String userDistinguishedName) {
        final var maybeUser = userRepository.findByDistinguishedNameIgnoreCase(userDistinguishedName);
        return maybeUser.orElseThrow(() -> new NoSuchUserException("Can't resolve user: " + userDistinguishedName));
    }

    @NationalUserOverride
    public List<User> findBySurnameIgnoreCaseAndForenameIgnoreCase(final String surname, final String forename) {
        return userRepository.findBySurnameIgnoreCaseAndForenameIgnoreCase(surname, forename);
    }

    @NationalUserOverride
    public List<User> findBySurnameIgnoreCase(final String surname) {
        return userRepository.findBySurnameIgnoreCase(surname);
    }

}
