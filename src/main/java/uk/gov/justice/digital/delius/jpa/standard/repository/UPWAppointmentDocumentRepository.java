package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.UPWAppointmentDocument;

import java.util.List;

public interface UPWAppointmentDocumentRepository extends JpaRepository<UPWAppointmentDocument, Long> {
    List<UPWAppointmentDocument> findByOffenderId(Long offenderId);
}
