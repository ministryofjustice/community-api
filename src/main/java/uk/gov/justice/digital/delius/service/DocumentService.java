package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.justice.digital.delius.data.api.ConvictionDocuments;
import uk.gov.justice.digital.delius.data.api.DocumentLink;
import uk.gov.justice.digital.delius.data.api.OffenderDocumentDetail;
import uk.gov.justice.digital.delius.data.api.OffenderDocuments;
import uk.gov.justice.digital.delius.jpa.national.repository.DocumentRepository;
import uk.gov.justice.digital.delius.jpa.oracle.annotations.NationalUserOverride;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;
import uk.gov.justice.digital.delius.jpa.standard.repository.*;
import uk.gov.justice.digital.delius.transformers.DocumentTransformer;

import java.time.LocalDateTime;
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
    private final DocumentTransformer documentTransformer;


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

    public OffenderDocuments offenderDocumentsFor(Long offenderId) {
        val eventDocuments = eventDocumentRepository.findByOffenderId(offenderId);
        val courtReportDocuments = courtReportDocumentRepository.findByOffenderId(offenderId);
        val institutionReportDocuments = institutionReportDocumentRepository.findByOffenderId(offenderId);
        val approvedPremisesReferralDocuments = approvedPremisesReferralDocumentRepository.findByOffenderId(offenderId);
        val assessmentDocuments = assessmentDocumentRepository.findByOffenderId(offenderId);
        val caseAllocationDocuments = caseAllocationDocumentRepository.findByOffenderId(offenderId);
        val referralDocuments = referralDocumentRepository.findByOffenderId(offenderId);
        val allNsiDocuments = nsiDocumentRepository.findByOffenderId(offenderId);
        val nsiEventDocuments = allNsiDocuments.stream().filter(this::isEventRelated).collect(toList());
        val upwAppointmentDocuments = upwAppointmentDocumentRepository.findByOffenderId(offenderId);
        val allContactDocuments = contactDocumentRepository.findByOffenderId(offenderId);
        val contactEventDocuments = allContactDocuments.stream().filter(this::isEventRelated).collect(toList());
        val events = eventRepository.findByOffenderId(offenderId);
        val offender = offenderRepository
                .findByOffenderId(offenderId)
                .orElseThrow(() -> new RuntimeException(String.format("offenderDocumentsFor could not find offender %d", offenderId)));

        val setOfRelatedEventIds = ImmutableSet
                .<Long>builder()
                .addAll(events.stream().filter(Event::hasCpsPack).map(Event::getEventId).collect(toList()))
                .addAll(eventDocuments.stream().map(this::eventId).collect(toList()))
                .addAll(courtReportDocuments.stream().map(this::eventId).collect(toList()))
                .addAll(institutionReportDocuments.stream().map(this::eventId).collect(toList()))
                .addAll(approvedPremisesReferralDocuments.stream().map(this::eventId).collect(toList()))
                .addAll(assessmentDocuments.stream().map(this::eventId).collect(toList()))
                .addAll(caseAllocationDocuments.stream().map(this::eventId).collect(toList()))
                .addAll(referralDocuments.stream().map(this::eventId).collect(toList()))
                .addAll(nsiEventDocuments.stream().map(this::eventId).collect(toList()))
                .addAll(upwAppointmentDocuments.stream().map(this::eventId).collect(toList()))
                .addAll(contactEventDocuments.stream().map(this::eventId).collect(toList()))
                .build();

        val convictions = setOfRelatedEventIds
                .stream()
                .map(eventId -> ConvictionDocuments
                        .builder()
                        .convictionId(String.valueOf(eventId))
                        .documents(
                                ImmutableList.<OffenderDocumentDetail>builder()
                                        .addAll(toOffenderDocumentDetailList(eventWithCPsPack(events, eventId)))
                                        .addAll(
                                            documentTransformer
                                            .offenderDocumentsDetailsOfEventDocuments(
                                                    eventDocuments
                                                            .stream()
                                                            .filter(document -> eventId(document).equals(eventId))
                                                            .collect(toList())))
                                        .addAll(documentTransformer
                                                .offenderDocumentsDetailsOfCourtReportDocuments(
                                                        courtReportDocuments
                                                                .stream()
                                                                .filter(document -> eventId(document).equals(eventId))
                                                                .collect(toList()))
                                        )
                                        .addAll(documentTransformer
                                                .offenderDocumentsDetailsOfInstitutionReportDocuments(
                                                        institutionReportDocuments
                                                                .stream()
                                                                .filter(document -> eventId(document).equals(eventId))
                                                                .collect(toList()))
                                        )
                                        .addAll(documentTransformer
                                                .offenderDocumentsDetailsOfContactDocuments(
                                                        contactEventDocuments
                                                                .stream()
                                                                .filter(document -> eventId(document).equals(eventId))
                                                                .collect(toList()))
                                        )
                                        .addAll(documentTransformer
                                                .offenderDocumentsDetailsOfApprovedPremisesReferralDocuments(
                                                        approvedPremisesReferralDocuments
                                                                .stream()
                                                                .filter(document -> eventId(document).equals(eventId))
                                                                .collect(toList()))
                                        )
                                        .addAll(documentTransformer
                                                .offenderDocumentsDetailsOfAssessmentDocuments(
                                                        assessmentDocuments
                                                                .stream()
                                                                .filter(document -> eventId(document).equals(eventId))
                                                                .collect(toList()))
                                        )
                                        .addAll(documentTransformer
                                                .offenderDocumentsDetailsOfCaseAllocationDocuments(
                                                        caseAllocationDocuments
                                                                .stream()
                                                                .filter(document -> eventId(document).equals(eventId))
                                                                .collect(toList()))
                                        )
                                        .addAll(documentTransformer
                                                .offenderDocumentsDetailsOfReferralDocuments(
                                                        referralDocuments
                                                                .stream()
                                                                .filter(document -> eventId(document).equals(eventId))
                                                                .collect(toList()))
                                        )
                                        .addAll(documentTransformer
                                                .offenderDocumentsDetailsOfNsiDocuments(
                                                        nsiEventDocuments
                                                                .stream()
                                                                .filter(document -> eventId(document).equals(eventId))
                                                                .collect(toList()))
                                        )
                                        .addAll(documentTransformer
                                                .offenderDocumentsDetailsOfUPWAppointmentDocuments(
                                                        upwAppointmentDocuments
                                                                .stream()
                                                                .filter(document -> eventId(document).equals(eventId))
                                                                .collect(toList()))
                                        )
                                        .build()
                        )
                        .build())
                .collect(toList());

        return OffenderDocuments
                .builder()
                .documents(
                        ImmutableList.<OffenderDocumentDetail>builder()
                                .addAll(previousConvictions(offender))
                                .addAll(documentTransformer
                                        .offenderDocumentsDetailsOfOffenderDocuments(
                                                offenderDocumentRepository.findByOffenderId(offenderId)))
                                .addAll(documentTransformer
                                        .offenderDocumentsDetailsOfAddressAssessmentDocuments(
                                                addressAssessmentDocumentRepository.findByOffenderId(offenderId)))
                                .addAll(documentTransformer
                                        .offenderDocumentsDetailsOfPersonalContactDocuments(
                                                personalContactDocumentRepository.findByOffenderId(offenderId)))
                                .addAll(documentTransformer
                                        .offenderDocumentsDetailsOfPersonalCircumstanceDocuments(
                                                personalCircumstanceDocumentRepository.findByOffenderId(offenderId)))
                                .addAll(documentTransformer
                                        .offenderDocumentsDetailsOfContactDocuments(
                                                allContactDocuments.stream().filter(not(this::isEventRelated)).collect(toList())))
                                .addAll(documentTransformer
                                        .offenderDocumentsDetailsOfNsiDocuments(
                                                allNsiDocuments.stream().filter(not(this::isEventRelated)).collect(toList())))
                                .build()
                )
                .convictions(convictions)
                .build();
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
        return Optional.ofNullable(document.getContact().getEvent()).map(Event::getEventId).orElseThrow(() -> new RuntimeException("requested eventid even when this is offender related"));
    }

    private Optional<Event> eventWithCPsPack(List<Event> events, Long eventId) {
        return events
                .stream()
                .filter(event -> event.getEventId().equals(eventId))
                .filter(Event::hasCpsPack)
                .findAny();
    }

    private List<OffenderDocumentDetail> toOffenderDocumentDetailList(Optional<Event> maybeEvent) {
        return maybeEvent
                .map(event -> ImmutableList.of(documentTransformer.offenderDocumentDetailsOfCpsPack(event)))
                .orElseGet(ImmutableList::of);
    }

    private List<OffenderDocumentDetail> previousConvictions(Offender offender) {
        return Optional.of(offender)
                .filter(this::hasPreviousConvictions)
                .map(offenderWithPreCPns -> ImmutableList.of(documentTransformer.offenderDocumentDetailsOfPreviousConvictions(offenderWithPreCPns)))
                .orElseGet(ImmutableList::of);
    }

    private boolean hasPreviousConvictions(Offender offender) {
        return !StringUtils.isEmpty(offender.getPreviousConvictionsAlfrescoDocumentId());
    }
}

