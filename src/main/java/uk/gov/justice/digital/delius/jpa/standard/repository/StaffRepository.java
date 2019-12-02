package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByStaffId(Long staffId);

    Optional<Staff> findByOfficerCode(String officerCode);

    @Query("select u.staff from User u where upper(u.distinguishedName) = upper(:username)")
    Optional<Staff> findByUsername(@Param("username") String username);

    Optional<Staff> findBySurnameAndForenameAndProbationArea(String surname, String forename, ProbationArea probationArea);
}
