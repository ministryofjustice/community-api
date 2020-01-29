package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.CustodyHistory;

public interface CustodyHistoryRepository extends JpaRepository<CustodyHistory, Long> {
}
