package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.DocumentLink;
import uk.gov.justice.digital.delius.jpa.national.repository.DocumentRepository;
import uk.gov.justice.digital.delius.jpa.oracle.annotations.NationalUserOverride;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final OffenderRepository offenderRepository;


    @Autowired
    public DocumentService(DocumentRepository documentRepository, OffenderRepository offenderRepository) {
        this.documentRepository = documentRepository;
        this.offenderRepository = offenderRepository;
    }

    @NationalUserOverride
    public void insertDocument(DocumentLink documentLink) {
        Long probationAreaId = documentRepository.lookupProbationArea(documentLink.getProbationAreaCode());

        Long userId = documentRepository.lookupUser(probationAreaId, documentLink.getAlfrescoUser());

        Optional<Offender> maybeOffender = offenderRepository.findByCrn(documentLink.getCrn());

        LocalDateTime now = LocalDateTime.now();

        uk.gov.justice.digital.delius.jpa.national.entity.Document documentEntity = uk.gov.justice.digital.delius.jpa.national.entity.Document.builder()
                .offenderId(maybeOffender.get().getOffenderId())
                .alfrescoId(documentLink.getAlfrescoId())
                .documentName(documentLink.getDocumentName())
                .status("N")
                .workInProgress("N")
                .tableName(documentLink.getTableName())
                .primaryKeyId(documentLink.getEntityId())
                .createdDate(now)
                .createdByProbationAreaId(probationAreaId)
                .lastUpdatedByProbationAreaId(probationAreaId)
                .userId(userId)
                .lastSaved(now)
                .build();

        documentRepository.save(documentEntity);
    }
}

