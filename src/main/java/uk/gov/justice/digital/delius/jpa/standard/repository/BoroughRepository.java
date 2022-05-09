package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.justice.digital.delius.jpa.standard.entity.Borough;

import java.util.Optional;

public interface BoroughRepository extends JpaRepository<Borough, Long> {
    @Query("""
    select b from Borough b
     where b.code = :code
     and (b.probationArea.endDate is null or b.probationArea.endDate > current_date)
    """)
    Optional<Borough> findActiveByCode(String code);
}
