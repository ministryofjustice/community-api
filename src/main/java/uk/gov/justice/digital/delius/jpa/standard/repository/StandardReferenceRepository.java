package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.util.Optional;

public interface StandardReferenceRepository extends JpaRepository<StandardReference, Long> {
    @Query("select sf from StandardReference sf inner join sf.referenceDataMaster rdm where rdm.codeSetName = :codeSetName and sf.codeValue = :code")
    Optional<StandardReference> findByCodeAndCodeSetName(@Param("code") String code, @Param("codeSetName") String codeSetName);
}
