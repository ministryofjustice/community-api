package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.data.api.UploadedDocumentCreateResponse;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewContact;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewDocument;
import uk.gov.justice.digital.delius.data.api.deliusapi.UploadedDocumentDto;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;

import static java.lang.String.format;

@Service
public class DeliusDocumentsService {
    private static final String CP_UPW = "EASU";

    private final DeliusApiClient deliusApiClient;
    private final ContactTypeRepository contactTypeRepository;

    @Autowired
    public DeliusDocumentsService(DeliusApiClient deliusApiClient, ContactTypeRepository contactTypeRepository) {
        this.deliusApiClient = deliusApiClient;
        this.contactTypeRepository = contactTypeRepository;
    }

    @Transactional
    public UploadedDocumentCreateResponse createDocument(String crn, Long eventId, String contactTypeCode, NewDocument document) {
        this.assertCompletedUPWAssessmentContactType(contactTypeCode);

        final var newContact = makeNewContact(crn, eventId, contactTypeCode);
        final var contactDto = deliusApiClient.createNewContact(newContact);

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

        if (type.getCode() != CP_UPW) {
            throw new BadRequestException(format("contact type '%s' is not a completed UPW assessment type", contactTypeCode));
        }
    }

    private NewContact makeNewContact(String crn, Long eventId, String contactType) {
        return NewContact.builder()
            .offenderCrn(crn)
            .type(contactType)
            /*.provider(request.getProviderCode())
            .team(request.getTeamCode())
            .staff(request.getStaffCode())
            .officeLocation(request.getOfficeLocationCode())
            .date(toLondonLocalDate(request.getAppointmentStart()))
            .startTime(toLondonLocalTime(request.getAppointmentStart()))
            .endTime(toLondonLocalTime(request.getAppointmentEnd()))*/
            //.notes(request.getNotes())
            //.nsiId(request.getNsiId())
            .eventId(eventId)
            //.requirementId(request.getRequirementId())
            //.sensitive(request.getSensitive())
            //.rarActivity(request.getRarActivity())
            .build();
    }


}
