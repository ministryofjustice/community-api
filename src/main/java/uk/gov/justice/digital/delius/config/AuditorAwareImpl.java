package uk.gov.justice.digital.delius.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.helpers.CurrentUserSupplier;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.jpa.national.repository.UserRepository;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@Service(value = "auditorAware")
public class AuditorAwareImpl implements AuditorAware<Long> {
    private final CurrentUserSupplier currentUserSupplier;
    private final UserRepository userRepository;

    public AuditorAwareImpl(CurrentUserSupplier currentUserSupplier, UserRepository userRepository) {
        this.currentUserSupplier = currentUserSupplier;
        this.userRepository = userRepository;
    }


    @Override
    public Optional<Long> getCurrentAuditor() {
        return currentUserSupplier.username()
                .flatMap(userRepository::findByDistinguishedNameIgnoreCase)
                .map(User::getUserId);
    }
}
