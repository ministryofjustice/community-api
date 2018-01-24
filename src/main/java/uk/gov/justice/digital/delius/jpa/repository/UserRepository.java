package uk.gov.justice.digital.delius.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.entity.Offender;
import uk.gov.justice.digital.delius.jpa.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByDistinguishedName(String distinguishedName);
}
