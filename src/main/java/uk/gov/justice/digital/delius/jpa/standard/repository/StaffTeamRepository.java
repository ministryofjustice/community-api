package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.StaffTeam;
import uk.gov.justice.digital.delius.jpa.standard.entity.StaffTeamPK;

public interface StaffTeamRepository extends JpaRepository<StaffTeam, StaffTeamPK> {
}
