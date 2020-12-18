package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.ReferralDocument;

import java.util.List;

public interface ReferralDocumentRepository extends JpaRepository<ReferralDocument, Long> {
    @Query("select document from ReferralDocument document, Referral entity where document.referral = entity and document.offenderId = :offenderId and document.softDeleted = false")
    List<ReferralDocument> findByOffenderId(@Param("offenderId") Long offenderId);
}
