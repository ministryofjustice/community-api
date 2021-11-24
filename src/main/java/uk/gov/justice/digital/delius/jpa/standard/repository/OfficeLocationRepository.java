package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.OfficeLocation;

import java.util.List;

public interface OfficeLocationRepository extends JpaRepository<OfficeLocation, Long> {
    @Query("""
        SELECT location FROM OfficeLocation location, Team team
        WHERE (location.endDate IS NULL OR location.endDate > CURRENT_DATE)
        AND team MEMBER OF location.teams
        AND team.code = :teamCode
        AND team.startDate <= CURRENT_DATE
        AND (team.endDate IS NULL OR team.endDate > CURRENT_DATE)
        """)
    List<OfficeLocation> findActiveOfficeLocationsForTeam(@Param("teamCode") final String teamCode);
}
