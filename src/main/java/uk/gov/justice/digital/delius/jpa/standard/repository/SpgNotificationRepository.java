package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.SpgNotification;

public interface SpgNotificationRepository extends JpaRepository<SpgNotification, Long> {
}
