package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.data.api.ConvictionDocuments;
import uk.gov.justice.digital.delius.data.api.DocumentLink;
import uk.gov.justice.digital.delius.data.api.OffenderDocumentDetail.Type;
import uk.gov.justice.digital.delius.data.api.OffenderDocuments;
import uk.gov.justice.digital.delius.data.filters.DocumentFilter;
import uk.gov.justice.digital.delius.jpa.filters.CourtReportDocumentFilterTransformer;
import uk.gov.justice.digital.delius.jpa.national.repository.DocumentRepository;
import uk.gov.justice.digital.delius.jpa.oracle.annotations.NationalUserOverride;
import uk.gov.justice.digital.delius.jpa.standard.entity.AddressAssessmentDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.ApprovedPremisesReferralDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.AssessmentDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.CaseAllocationDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtReportDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.Document.DocumentType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.EventDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReportDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.NsiDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.PersonalCircumstanceDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.PersonalContactDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.ReferralDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.UPWAppointmentDocument;
import uk.gov.justice.digital.delius.jpa.standard.repository.AddressAssessmentDocumentRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ApprovedPremisesReferralDocumentRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.AssessmentDocumentRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.CaseAllocationDocumentRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactDocumentRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtReportDocumentRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventDocumentRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.InstitutionReportDocumentRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.NsiDocumentRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderDocumentRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.PersonalCircumstanceDocumentRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.PersonalContactDocumentRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ReferralDocumentRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.UPWAppointmentDocumentRepository;
import uk.gov.justice.digital.delius.transformers.DocumentTransformer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.helpers.FluentHelper.not;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final OffenderRepository offenderRepository;
    private final OffenderDocumentRepository offenderDocumentRepository;
    private final EventDocumentRepository eventDocumentRepository;
    private final CourtReportDocumentRepository courtReportDocumentRepository;
    private final InstitutionReportDocumentRepository institutionReportDocumentRepository;
    private final EventRepository eventRepository;
    private final AddressAssessmentDocumentRepository addressAssessmentDocumentRepository;
    private final ApprovedPremisesReferralDocumentRepository approvedPremisesReferralDocumentRepository;
    private final AssessmentDocumentRepository assessmentDocumentRepository;
    private final CaseAllocationDocumentRepository caseAllocationDocumentRepository;
    private final PersonalContactDocumentRepository personalContactDocumentRepository;
    private final ReferralDocumentRepository referralDocumentRepository;
    private final NsiDocumentRepository nsiDocumentRepository;
    private final PersonalCircumstanceDocumentRepository personalCircumstanceDocumentRepository;
    private final UPWAppointmentDocumentRepository upwAppointmentDocumentRepository;
    private final ContactDocumentRepository contactDocumentRepository;


    @NationalUserOverride
    public void insertDocument(DocumentLink documentLink) {
        Long probationAreaId = documentRepository.lookupProbationArea(documentLink.getProbationAreaCode());

        Long userId = documentRepository.lookupUser(probationAreaId, documentLink.getAlfrescoUser());

        Optional<Offender> maybeOffender = offenderRepository.findByCrn(documentLink.getCrn());

        LocalDateTime now = LocalDateTime.now();

        uk.gov.justice.digital.delius.jpa.national.entity.Document documentEntity = uk.gov.justice.digital.delius.jpa.national.entity.Document.builder()
                .offenderId(maybeOffender.orElseThrow(() -> new RuntimeException(String.format("insertDocument could not find offender %s", documentLink.getCrn()))).getOffenderId())
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

    @Transactional(readOnly = true)
    public OffenderDocuments offenderDocumentsFor(Long offenderId, DocumentFilter filter) {

        final var eventDocuments = eventDocumentsFor(offenderId, filter);
        final var cpsDocuments = cpsDocumentsFor(offenderId, filter);
        final var courtReportDocuments = courtReportDocumentsFor(offenderId, filter);
        final var institutionReportDocuments = institutionReportDocumentsFor(offenderId, filter);
        final var approvedPremisesReferralDocuments = approvedPremisesReferralDocumentsFor(offenderId, filter);
        final var assessmentDocuments = assessmentDocumentsFor(offenderId, filter);
        final var caseAllocationDocuments = caseAllocationDocumentsFor(offenderId, filter);
        final var referralDocuments = referralDocumentsFor(offenderId, filter);
        final var allNsiDocuments = nsiDocumentsFor(offenderId, filter);
        final var nsiEventDocuments = allNsiDocuments.stream().filter(this::isEventRelated).toList();
        final var upwAppointmentDocuments = upwAppointmentDocumentsFor(offenderId, filter);
        final var allContactDocuments = contactDocumentsFor(offenderId, filter);
        final var contactEventDocuments = allContactDocuments.stream().filter(this::isEventRelated).toList();

        final var setOfRelatedEventIds = new HashSet<>();

        setOfRelatedEventIds.addAll(eventDocuments.stream().map(this::eventId).toList());
        setOfRelatedEventIds.addAll(cpsDocuments.stream().map(this::eventId).toList());
        setOfRelatedEventIds.addAll(courtReportDocuments.stream().map(this::eventId).toList());
        setOfRelatedEventIds.addAll(institutionReportDocuments.stream().map(this::eventId).toList());
        setOfRelatedEventIds.addAll(approvedPremisesReferralDocuments.stream().map(this::eventId).toList());
        setOfRelatedEventIds.addAll(assessmentDocuments.stream().filter(d -> d.getAssessment().getReferral() != null).map(this::eventId).toList());
        setOfRelatedEventIds.addAll(caseAllocationDocuments.stream().map(this::eventId).toList());
        setOfRelatedEventIds.addAll(referralDocuments.stream().map(this::eventId).toList());
        setOfRelatedEventIds.addAll(nsiEventDocuments.stream().map(this::eventId).toList());
        setOfRelatedEventIds.addAll(upwAppointmentDocuments.stream().map(this::eventId).toList());
        setOfRelatedEventIds.addAll(contactEventDocuments.stream().map(this::eventId).toList());

        final var convictions = setOfRelatedEventIds
                .stream()
                .map(eventId -> ConvictionDocuments
                        .builder()
                        .convictionId(String.valueOf(eventId))
                        .documents(
                            combine(
                                        DocumentTransformer
                                            .offenderDocumentsDetailsOfEventDocuments(
                                                    eventDocuments
                                                            .stream()
                                                            .filter(document -> eventId(document).equals(eventId))
                                                            .collect(toList())),
                                        DocumentTransformer
                                                .offenderDocumentsDetailsOfEventDocuments(
                                                    cpsDocuments
                                                        .stream()
                                                        .filter(document -> eventId(document).equals(eventId))
                                                        .collect(toList())),
                                        DocumentTransformer
                                                .offenderDocumentsDetailsOfCourtReportDocuments(
                                                        courtReportDocuments
                                                                .stream()
                                                                .filter(document -> eventId(document).equals(eventId))
                                                                .collect(toList())),
                                        DocumentTransformer
                                                .offenderDocumentsDetailsOfInstitutionReportDocuments(
                                                        institutionReportDocuments
                                                                .stream()
                                                                .filter(document -> eventId(document).equals(eventId))
                                                                .collect(toList())),
                                        DocumentTransformer
                                                .offenderDocumentsDetailsOfContactDocuments(
                                                        contactEventDocuments
                                                                .stream()
                                                                .filter(document -> eventId(document).equals(eventId))
                                                                .collect(toList())),
                                        DocumentTransformer
                                                .offenderDocumentsDetailsOfApprovedPremisesReferralDocuments(
                                                        approvedPremisesReferralDocuments
                                                                .stream()
                                                                .filter(document -> eventId(document).equals(eventId))
                                                                .collect(toList())),
                                        DocumentTransformer
                                                .offenderDocumentsDetailsOfAssessmentDocuments(
                                                        assessmentDocuments
                                                                .stream()
                                                                .filter(d -> d.getAssessment().getReferral() != null)
                                                                .filter(document -> eventId(document).equals(eventId))
                                                                .collect(toList())),
                                        DocumentTransformer
                                                .offenderDocumentsDetailsOfCaseAllocationDocuments(
                                                        caseAllocationDocuments
                                                                .stream()
                                                                .filter(document -> eventId(document).equals(eventId))
                                                                .collect(toList())),
                                        DocumentTransformer
                                                .offenderDocumentsDetailsOfReferralDocuments(
                                                        referralDocuments
                                                                .stream()
                                                                .filter(document -> eventId(document).equals(eventId))
                                                                .collect(toList())),
                                        DocumentTransformer
                                                .offenderDocumentsDetailsOfNsiDocuments(
                                                        nsiEventDocuments
                                                                .stream()
                                                                .filter(document -> eventId(document).equals(eventId))
                                                                .collect(toList())),
                                        DocumentTransformer
                                                .offenderDocumentsDetailsOfUPWAppointmentDocuments(
                                                        upwAppointmentDocuments
                                                                .stream()
                                                                .filter(document -> eventId(document).equals(eventId))
                                                                .collect(toList()))
                                        )
                        )
                        .build())
                .collect(toList());
        return OffenderDocuments
                .builder()
                .documents(
                        combine(
                                DocumentTransformer
                                        .offenderDocumentsDetailsOfOffenderDocuments(
                                            offenderRelatedDocumentsFor(offenderId, filter)),
                                DocumentTransformer
                                .offenderDocumentsDetailsOfOffenderDocuments(
                                        offenderPreConsDocumentFor(offenderId, filter)),
                                DocumentTransformer
                                        .offenderDocumentsDetailsOfAddressAssessmentDocuments(
                                            addressAssessmentDocumentsFor(offenderId, filter)),
                                DocumentTransformer
                                        .offenderDocumentsDetailsOfPersonalContactDocuments(
                                            personalContactDocumentsFor(offenderId, filter)),
                                DocumentTransformer
                                        .offenderDocumentsDetailsOfPersonalCircumstanceDocuments(
                                            personalCircumstanceDocumentsFor(offenderId, filter)),
                                DocumentTransformer
                                        .offenderDocumentsDetailsOfContactDocuments(
                                                allContactDocuments.stream().filter(not(this::isEventRelated)).collect(toList())),
                                DocumentTransformer
                                        .offenderDocumentsDetailsOfNsiDocuments(
                                                allNsiDocuments.stream().filter(not(this::isEventRelated)).collect(toList())))
                )
                .convictions(convictions)
                .build();
    }

    private List<PersonalCircumstanceDocument> personalCircumstanceDocumentsFor(Long offenderId, DocumentFilter filter) {
        return filter.documentsFor(Type.PERSONAL_CIRCUMSTANCE_DOCUMENT, () -> personalCircumstanceDocumentRepository.findByOffenderId(offenderId));
    }

    private List<PersonalContactDocument> personalContactDocumentsFor(Long offenderId, DocumentFilter filter) {
        return filter.documentsFor(Type.PERSONAL_CONTACT_DOCUMENT, () -> personalContactDocumentRepository.findByOffenderId(offenderId));
    }

    private List<AddressAssessmentDocument> addressAssessmentDocumentsFor(Long offenderId, DocumentFilter filter) {
        return filter.documentsFor(Type.ADDRESS_ASSESSMENT_DOCUMENT, () -> addressAssessmentDocumentRepository.findByOffenderId(offenderId));
    }

    private List<OffenderDocument> offenderRelatedDocumentsFor(Long offenderId, DocumentFilter filter) {
        return filter.documentsFor(Type.OFFENDER_DOCUMENT, () -> offenderDocumentRepository.findByOffenderId(offenderId, DocumentType.DOCUMENT));
    }

    private List<OffenderDocument> offenderPreConsDocumentFor(Long offenderId, DocumentFilter filter) {
        return filter.documentsFor(Type.PRECONS_DOCUMENT, () -> offenderDocumentRepository.findByOffenderId(offenderId, DocumentType.PREVIOUS_CONVICTION));
    }
    private List<ContactDocument> contactDocumentsFor(Long offenderId, DocumentFilter filter) {
        return filter.documentsFor(Type.CONTACT_DOCUMENT, () -> contactDocumentRepository.findByOffenderId(offenderId));
    }

    private List<UPWAppointmentDocument> upwAppointmentDocumentsFor(Long offenderId, DocumentFilter filter) {
        return filter.documentsFor(Type.UPW_APPOINTMENT_DOCUMENT, () -> upwAppointmentDocumentRepository.findByOffenderId(offenderId));
    }

    private List<NsiDocument> nsiDocumentsFor(Long offenderId, DocumentFilter filter) {
        return filter.documentsFor(Type.NSI_DOCUMENT, () -> nsiDocumentRepository.findByOffenderId(offenderId));
    }

    private List<ReferralDocument> referralDocumentsFor(Long offenderId, DocumentFilter filter) {
        return filter.documentsFor(Type.REFERRAL_DOCUMENT, () -> referralDocumentRepository.findByOffenderId(offenderId));
    }

    private List<CaseAllocationDocument> caseAllocationDocumentsFor(Long offenderId, DocumentFilter filter) {
        return filter.documentsFor(Type.CASE_ALLOCATION_DOCUMENT, () -> caseAllocationDocumentRepository.findByOffenderId(offenderId));
    }

    private List<AssessmentDocument> assessmentDocumentsFor(Long offenderId, DocumentFilter filter) {
        return filter.documentsFor(Type.ASSESSMENT_DOCUMENT, () -> assessmentDocumentRepository.findByOffenderId(offenderId));
    }

    private List<ApprovedPremisesReferralDocument> approvedPremisesReferralDocumentsFor(Long offenderId, DocumentFilter filter) {
        return filter.documentsFor(Type.APPROVED_PREMISES_REFERRAL_DOCUMENT, () -> approvedPremisesReferralDocumentRepository.findByOffenderId(offenderId));
    }

    private List<InstitutionalReportDocument> institutionReportDocumentsFor(Long offenderId, DocumentFilter filter) {
        return filter.documentsFor(Type.INSTITUTION_REPORT_DOCUMENT, () -> institutionReportDocumentRepository.findByOffenderId(offenderId));
    }

    private List<CourtReportDocument> courtReportDocumentsFor(Long offenderId, DocumentFilter filter) {
        return filter.documentsFor(Type.COURT_REPORT_DOCUMENT, type -> courtReportDocumentRepository.findAll(CourtReportDocumentFilterTransformer.of(offenderId, type)));
    }

    private List<EventDocument> eventDocumentsFor(Long offenderId, DocumentFilter filter) {
        return filter.documentsFor(Type.CONVICTION_DOCUMENT, () -> eventDocumentRepository.findByOffenderId(offenderId, DocumentType.DOCUMENT));
    }

    private List<EventDocument> cpsDocumentsFor(Long offenderId, DocumentFilter filter) {
        return filter.documentsFor(Type.CPSPACK_DOCUMENT, () -> eventDocumentRepository.findByOffenderId(offenderId, DocumentType.CPS_PACK));
    }
    private boolean isEventRelated(ContactDocument contactDocument) {
        return Optional.ofNullable(contactDocument.getContact().getEvent()).isPresent();
    }

    private boolean isEventRelated(NsiDocument nsiDocument) {
        return Optional.ofNullable(nsiDocument.getNsi().getEvent()).isPresent();
    }

    private Long eventId(EventDocument document) {
        return document.getEvent().getEventId();
    }

    private Long eventId(InstitutionalReportDocument document) {
        return document.getInstitutionalReport().getCustody().getDisposal().getEvent().getEventId();
    }

    private Long eventId(CourtReportDocument document) {
        return document.getCourtReport().getCourtAppearance().getEvent().getEventId();
    }

    private Long eventId(ApprovedPremisesReferralDocument document) {
        return document.getApprovedPremisesReferral().getEvent().getEventId();
    }

    private Long eventId(AssessmentDocument document) {
        return document.getAssessment().getReferral().getEvent().getEventId();
    }

    private Long eventId(CaseAllocationDocument document) {
        return document.getCaseAllocation().getEvent().getEventId();
    }

    private Long eventId(ReferralDocument document) {
        return document.getReferral().getEvent().getEventId();
    }

    private Long eventId(NsiDocument document) {
        return document.getNsi().getEvent().getEventId();
    }

    private Long eventId(UPWAppointmentDocument document) {
        return document.getUpwAppointment().getUpwDetails().getDisposal().getEvent().getEventId();
    }
    private Long eventId(ContactDocument document) {
        return Optional.ofNullable(document.getContact().getEvent()).map(Event::getEventId).orElseThrow(() -> new RuntimeException("requested eventId even when this is offender related"));
    }

    @SafeVarargs
    private <T> List<T> combine(List<T>... values) {
        return Arrays.stream(values).flatMap(List::stream).toList();
    }
}

