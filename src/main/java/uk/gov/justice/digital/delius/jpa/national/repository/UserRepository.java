package uk.gov.justice.digital.delius.jpa.national.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.national.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByDistinguishedNameIgnoreCase(String distinguishedName);
    List<User> findBySurnameIgnoreCaseAndForenameIgnoreCase(String surname, String forename);
    List<User> findBySurnameIgnoreCase(String surname);
}
