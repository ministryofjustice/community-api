package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.UPWAppointmentDocument;

import java.util.List;

public interface UPWAppointmentDocumentRepository extends JpaRepository<UPWAppointmentDocument, Long> {
    @Query("select document from UPWAppointmentDocument document, UpwAppointment entity where document.upwAppointment = entity and document.offenderId = :offenderId and document.softDeleted = false")
    List<UPWAppointmentDocument> findByOffenderId(@Param("offenderId") Long offenderId);
}
