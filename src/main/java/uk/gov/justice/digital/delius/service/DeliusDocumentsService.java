package uk.gov.justice.digital.delius.service;

import static java.lang.String.format;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.UploadedDocumentCreateResponse;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewContact;
import uk.gov.justice.digital.delius.data.api.deliusapi.UploadedDocumentDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class DeliusDocumentsService {
    private static final String EASU = "EASU";
    private final DeliusApiClient deliusApiClient;
    private final OffenderManagerService offenderManagerService;

    @Autowired
    public DeliusDocumentsService(
        DeliusApiClient deliusApiClient,
        OffenderManagerService offenderManagerService) {
        this.deliusApiClient = deliusApiClient;
        this.offenderManagerService = offenderManagerService;
    }

    public UploadedDocumentCreateResponse createUPWDocument(String crn, Long convictionId, MultipartFile document) {
        final var newContact= makeNewUPWContact(crn, convictionId);
        final var contactDto= deliusApiClient.createNewContact(newContact);
        UploadedDocumentDto uploadedDocumentDto = deliusApiClient.uploadDocument(crn, contactDto.getId(), document);
        return makeResponse(uploadedDocumentDto);
    }

    private UploadedDocumentCreateResponse makeResponse(UploadedDocumentDto uploadedDocumentDto) {
        return new UploadedDocumentCreateResponse(
            uploadedDocumentDto.getId(),
            uploadedDocumentDto.getDocumentName(),
            uploadedDocumentDto.getCrn(),
            uploadedDocumentDto.getDateLastModified(),
            uploadedDocumentDto.getLastModifiedUser(),
            uploadedDocumentDto.getCreationDate()
        );
    }

    private NewContact makeNewUPWContact(String crn, Long convictionId) {
        CommunityOrPrisonOffenderManager offenderManager = getActiveOffenderManager(crn);
        return NewContact.builder()
            .offenderCrn(crn)
            .type(EASU)
            .provider(offenderManager.getProbationArea().getCode())
            .team(offenderManager.getTeam().getCode())
            .staff(offenderManager.getStaffCode())
            .date(LocalDate.now())
            .startTime(LocalTime.now())
            .eventId(convictionId)
            .build();
    }

    private CommunityOrPrisonOffenderManager getActiveOffenderManager(String crn) {
        return getAllOffenderManagers(crn)
            .stream()
            .filter(CommunityOrPrisonOffenderManager::getIsResponsibleOfficer)
            .findAny()
            .orElseThrow(() -> new NotFoundException(format("No active Offender Manager found for crn %s", crn)));
    }

    private List<CommunityOrPrisonOffenderManager> getAllOffenderManagers(String crn) {
        return offenderManagerService.getAllOffenderManagersForCrn(crn, true)
            .orElseThrow(() -> new NotFoundException(format("Offender Managers not found for crn %s", crn)));
    }
}
