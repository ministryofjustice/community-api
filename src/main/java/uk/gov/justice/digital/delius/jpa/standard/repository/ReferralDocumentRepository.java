package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.ReferralDocument;

import java.util.List;

public interface ReferralDocumentRepository extends JpaRepository<ReferralDocument, Long> {
    List<ReferralDocument> findByOffenderId(Long offenderId);
}
