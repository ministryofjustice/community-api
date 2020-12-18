package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.ApprovedPremisesReferralDocument;

import java.util.List;

public interface ApprovedPremisesReferralDocumentRepository extends JpaRepository<ApprovedPremisesReferralDocument, Long> {
    @Query("select document from ApprovedPremisesReferralDocument document, ApprovedPremisesReferral entity where document.approvedPremisesReferral = entity and document.offenderId = :offenderId and document.softDeleted = 0")
    List<ApprovedPremisesReferralDocument> findByOffenderId(@Param("offenderId") Long offenderId);
}
