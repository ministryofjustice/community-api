package uk.gov.justice.digital.delius.service;

import static java.lang.String.format;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.OffenderManager;
import uk.gov.justice.digital.delius.data.api.UploadedDocumentCreateResponse;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewContact;
import uk.gov.justice.digital.delius.data.api.deliusapi.UploadedDocumentDto;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;
import static uk.gov.justice.digital.delius.utils.DateConverter.toLondonLocalDate;
import static uk.gov.justice.digital.delius.utils.DateConverter.toLondonLocalTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class DeliusDocumentsService {
    private static final String CP_UPW = "EASU";

    private final DeliusApiClient deliusApiClient;
    private final ContactTypeRepository contactTypeRepository;
    private final OffenderManagerService offenderManagerService;

    @Autowired
    public DeliusDocumentsService(
        DeliusApiClient deliusApiClient,
        ContactTypeRepository contactTypeRepository,
        OffenderManagerService offenderManagerService) {
        this.deliusApiClient = deliusApiClient;
        this.contactTypeRepository = contactTypeRepository;
        this.offenderManagerService = offenderManagerService;
    }

    @Transactional
    public UploadedDocumentCreateResponse createDocument(String crn, Long eventId, String contactTypeCode, MultipartFile document) {
        this.assertCompletedUPWAssessmentContactType(contactTypeCode);

        final var newContact= makeNewContact(crn, eventId, contactTypeCode);
        final var contactDto= deliusApiClient.createNewContact(newContact);

        UploadedDocumentDto uploadedDocumentDto = deliusApiClient.uploadDocument(crn, contactDto.getId(), document);
        return makeResponse(uploadedDocumentDto);
    }

    private UploadedDocumentCreateResponse makeResponse(UploadedDocumentDto uploadedDocumentDto) {
        return new UploadedDocumentCreateResponse(
            uploadedDocumentDto.getId(),
            uploadedDocumentDto.getDocumentName(),
            uploadedDocumentDto.getCrn(),
            uploadedDocumentDto.getAuthor(),
            uploadedDocumentDto.getDateLastModified(),
            uploadedDocumentDto.getLastModifiedUser(),
            uploadedDocumentDto.getCreationDate()
        );
    }

    private void assertCompletedUPWAssessmentContactType(String contactTypeCode) {
        final var type = this.contactTypeRepository.findByCode(contactTypeCode)
            .orElseThrow(() -> new BadRequestException(format("contact type '%s' does not exist", contactTypeCode)));

        if (!type.getCode().equals(CP_UPW)) {
            throw new BadRequestException(format("contact type '%s' is not a completed UPW assessment type", contactTypeCode));
        }
    }

    private NewContact makeNewContact(String crn, Long eventId, String contactType) {

        CommunityOrPrisonOffenderManager offenderManager = getActiveOffenderManager(crn);
        return NewContact.builder()
            .offenderCrn(crn)
            .type(contactType)
            .provider(offenderManager.getProbationArea().getCode())
            .team(offenderManager.getTeam().getCode())
            .staff(offenderManager.getStaffCode())
            .date(LocalDate.now())
            .startTime(LocalTime.now())
            .eventId(eventId)
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
