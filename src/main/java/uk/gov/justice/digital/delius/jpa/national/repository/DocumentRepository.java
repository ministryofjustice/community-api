package uk.gov.justice.digital.delius.jpa.national.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.national.entity.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query(value = "select probation_area_id from probation_area where code = ?1", nativeQuery = true)
    Long lookupProbationArea(String code);

    @Query(value = "SELECT PKG_USER_SUPPORT.getSPGInboundUserID(?2,?1) FROM dual", nativeQuery = true)
    Long lookupUser(Long probationAreaId, String alfrescoUser);

}
