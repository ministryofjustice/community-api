package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByCode(String code);
    boolean existsByCode(String code);

    @Query("""
        SELECT team FROM Team team
        WHERE team.code = :code
        AND team.startDate <= CURRENT_DATE
        AND (team.endDate IS NULL OR team.endDate > CURRENT_DATE)
        """)
    Optional<Team> findActiveByCode(@Param("code") String code);
}
