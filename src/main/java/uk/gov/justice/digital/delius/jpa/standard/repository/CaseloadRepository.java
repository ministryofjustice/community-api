package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.Caseload;

import java.util.Collection;
import java.util.List;

public interface CaseloadRepository extends JpaRepository<Caseload, Long> {
    List<Caseload> findByStaffStaffIdAndRoleCodeIn(Long staffId, Collection<String> roles);
    List<Caseload> findByTeamCodeAndRoleCodeIn(String teamCode, Collection<String> roles);
}