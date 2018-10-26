package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.PersonalCircumstance;

import java.util.List;

public interface PersonalCircumstanceRepository extends JpaRepository<PersonalCircumstance, Long> {
    List<PersonalCircumstance> findByOffenderId(Long offenderId);
}
