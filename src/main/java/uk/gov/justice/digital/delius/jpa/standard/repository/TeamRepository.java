package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByCode(String code);

    @Query("""
SELECT DISTINCT team FROM Team team
LEFT JOIN FETCH team.officeLocations location
WHERE team.code = :code
  AND team.startDate <= CURRENT_DATE
  AND (team.endDate IS NULL OR team.endDate > CURRENT_DATE)
  AND (location.endDate IS NULL OR location.endDate > CURRENT_DATE)
""")
    Optional<Team> findActiveWithActiveOfficeLocationByCode(@Param("code") String code);
}
