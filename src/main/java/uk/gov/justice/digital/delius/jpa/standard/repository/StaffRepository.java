package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByStaffId(Long staffId);

    Optional<Staff> findByOfficerCode(String officerCode);

    List<Staff> findByOfficerCodeIn(Set<String> officerCodes);

    @Query("select u.staff from User u where upper(u.distinguishedName) = upper(:username)")
    Optional<Staff> findByUsername(@Param("username") String username);

    @Query("select u.staff from User u where upper(u.distinguishedName) in (:usernames)")
    List<Staff> findByUsernames(@Param("usernames") Set<String> usernames);

    Optional<Staff> findFirstBySurnameIgnoreCaseAndForenameIgnoreCaseAndProbationArea(String surname, String forename, ProbationArea probationArea);

    @Query("select staff from Staff staff, StaffTeam staffTeam, Team team where staff.officerCode like '%U' and staffTeam.staffId = staff.staffId and staffTeam.teamId = :teamId" )
    Optional<Staff> findByUnallocatedByTeam(@Param("teamId") Long teamId);

    @Query("select s.staffId from Staff s where s.officerCode = :officerCode")
    Optional<Long> findStaffIdByOfficerCode(String officerCode);

    @Query("select staff " +
        "from Staff staff, StaffTeam staffTeam " +
        "where staffTeam.staffId = staff.staffId " +
        "and staffTeam.teamId = :teamId " +
        "and (staff.endDate is null or staff.endDate > CURRENT_DATE) ")
    List<Staff> findStaffByTeamId(Long teamId);
}
