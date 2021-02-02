package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.justice.digital.delius.jpa.standard.entity.Registration;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByOffenderId(Long offenderId);

    @Query("select registration from Registration registration " +
        "where registration.registerType.code = 'MAPP' " +
        "and registration.offenderId = :offenderId " +
        "and registration.softDeleted = 0 " +
        "and registration.deregistered = 0 ")
    Optional<Registration> findActiveMappaRegistrationByOffenderId(Long offenderId);
}
