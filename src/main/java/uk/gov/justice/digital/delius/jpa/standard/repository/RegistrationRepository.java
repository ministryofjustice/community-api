package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.justice.digital.delius.jpa.standard.entity.Registration;

import java.util.List;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByOffenderId(Long offenderId);

    @Query("select registration from Registration registration " +
        "where registration.offenderId = :offenderId " +
        "and registration.softDeleted = 0 " +
        "and registration.deregistered = 0 ")
    List<Registration> findActiveByOffenderId(Long offenderId);

    @Query("select registration from Registration registration " +
        "where registration.registerType.code = 'MAPP' " +
        "and registration.offenderId = :offenderId " +
        "and registration.softDeleted = 0 " +
        "and registration.deregistered = 0 " +
        "order by registration.createdDatetime desc")
    Page<Registration>findActiveMappaRegistrationByOffenderId(Long offenderId, Pageable pageable);
}
