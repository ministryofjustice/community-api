package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.OffenderDocumentDetail;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Component
public class DocumentTransformer {
    public List<OffenderDocumentDetail> offenderDocumentsDetailsOfOffenderDocuments(List<OffenderDocument> documents) {
        return documents
                .stream()
                .map(this::offenderDocumentDetailOf)
                .collect(toList());
    }
    public List<OffenderDocumentDetail> offenderDocumentsDetailsOfEventDocuments(List<EventDocument> documents) {
        return documents
                .stream()
                .map(this::offenderDocumentDetailOf)
                .collect(toList());
    }
    public List<OffenderDocumentDetail> offenderDocumentsDetailsOfCourtReportDocuments(List<CourtReportDocument> documents) {
        return documents
                .stream()
                .map(this::offenderDocumentDetailOf)
                .collect(toList());
    }
    public List<OffenderDocumentDetail> offenderDocumentsDetailsOfInstitutionReportDocuments(List<InstitutionalReportDocument> documents) {
        return documents
                .stream()
                .map(this::offenderDocumentDetailOf)
                .collect(toList());
    }

    public List<OffenderDocumentDetail> offenderDocumentsDetailsOfAddressAssessmentDocuments(List<AddressAssessmentDocument> documents) {
        return documents
                .stream()
                .map(this::offenderDocumentDetailOf)
                .collect(toList());
    }

    public List<OffenderDocumentDetail> offenderDocumentsDetailsOfApprovedPremisesReferralDocuments(List<ApprovedPremisesReferralDocument> documents) {
        return documents
                .stream()
                .map(this::offenderDocumentDetailOf)
                .collect(toList());
    }

    public List<OffenderDocumentDetail> offenderDocumentsDetailsOfAssessmentDocuments(List<AssessmentDocument> documents) {
        return documents
                .stream()
                .map(this::offenderDocumentDetailOf)
                .collect(toList());
    }

    public List<OffenderDocumentDetail> offenderDocumentsDetailsOfCaseAllocationDocuments(List<CaseAllocationDocument> documents) {
        return documents
                .stream()
                .map(this::offenderDocumentDetailOf)
                .collect(toList());
    }


    public List<OffenderDocumentDetail> offenderDocumentsDetailsOfPersonalContactDocuments(List<PersonalContactDocument> documents) {
        return documents
                .stream()
                .map(this::offenderDocumentDetailOf)
                .collect(toList());
    }

    public List<OffenderDocumentDetail> offenderDocumentsDetailsOfReferralDocuments(List<ReferralDocument> documents) {
        return documents
                .stream()
                .map(this::offenderDocumentDetailOf)
                .collect(toList());
    }


    public List<OffenderDocumentDetail> offenderDocumentsDetailsOfNsiDocuments(List<NsiDocument> documents) {
        return documents
                .stream()
                .map(this::offenderDocumentDetailOf)
                .collect(toList());
    }


    public List<OffenderDocumentDetail> offenderDocumentsDetailsOfPersonalCircumstanceDocuments(List<PersonalCircumstanceDocument> documents) {
        return documents
                .stream()
                .map(this::offenderDocumentDetailOf)
                .collect(toList());
    }


    public List<OffenderDocumentDetail> offenderDocumentsDetailsOfUPWAppointmentDocuments(List<UPWAppointmentDocument> documents) {
        return documents
                .stream()
                .map(this::offenderDocumentDetailOf)
                .collect(toList());
    }


    public List<OffenderDocumentDetail> offenderDocumentsDetailsOfContactDocuments(List<ContactDocument> documents) {
        return documents
                .stream()
                .map(this::offenderDocumentDetailOf)
                .collect(toList());
    }

    public OffenderDocumentDetail offenderDocumentDetailsOfCpsPack(Event event) {
        return OffenderDocumentDetail
                .builder()
                .author(Optional.ofNullable(event.getCpsCreatedByUser())
                        .map(this::fullName)
                        .orElse(null))
                .createdAt(event.getCpsCreatedDatetime())
                .documentName(event.getCpsDocumentName())
                .id(event.getCpsAlfrescoDocumentId())
                .type(KeyValue
                        .builder()
                        .code(OffenderDocumentDetail.Type.CPSPACK_DOCUMENT.name())
                        .description(OffenderDocumentDetail.Type.CPSPACK_DOCUMENT.getDescription())
                        .build())
                .extendedDescription(String.format("Crown Prosecution Service case pack for %s", toHumanReadable(event.getCpsDate())))
                .build();

    }

    public OffenderDocumentDetail offenderDocumentDetailsOfPreviousConvictions(Offender offender) {
        
        return OffenderDocumentDetail
                .builder()
                .author(Optional.ofNullable(offender.getPreviousConvictionsCreatedByUser())
                                .map(this::fullName)
                                .orElse(null))
                .createdAt(offender.getPreviousConvictionsCreatedDatetime())
                .documentName(offender.getPrevConvictionDocumentName())
                .id(offender.getPreviousConvictionsAlfrescoDocumentId())
                .type(KeyValue
                        .builder()
                        .code(OffenderDocumentDetail.Type.PRECONS_DOCUMENT.name())
                        .description(OffenderDocumentDetail.Type.PRECONS_DOCUMENT.getDescription())
                        .build())
                .extendedDescription(String.format("Previous convictions as of %s", toHumanReadable(offender.getPreviousConvictionDate())))
                .build();
    }

    private OffenderDocumentDetail offenderDocumentDetailOf(OffenderDocument document) {
        return offenderDocumentDetailBuilderOf(document)
                .type(KeyValue
                        .builder()
                        .code(OffenderDocumentDetail.Type.OFFENDER_DOCUMENT.name())
                        .description(OffenderDocumentDetail.Type.OFFENDER_DOCUMENT.getDescription())
                        .build())
                .build();
    }
    private OffenderDocumentDetail offenderDocumentDetailOf(EventDocument document) {
        return offenderDocumentDetailBuilderOf(document)
                .type(KeyValue
                        .builder()
                        .code(OffenderDocumentDetail.Type.CONVICTION_DOCUMENT.name())
                        .description(OffenderDocumentDetail.Type.CONVICTION_DOCUMENT.getDescription())
                        .build())
                .build();
    }

    private OffenderDocumentDetail offenderDocumentDetailOf(CourtReportDocument document) {
        return offenderDocumentDetailBuilderOf(document)
                .extendedDescription(String.format(
                        "%s requested by %s on %s",
                        document.getCourtReport().getCourtReportType().getDescription(),
                        document.getCourtReport().getCourtAppearance().getCourt().getCourtName(),
                        toHumanReadable(document.getCourtReport().getDateRequested())
                ))
                .type(KeyValue
                        .builder()
                        .code(OffenderDocumentDetail.Type.COURT_REPORT_DOCUMENT.name())
                        .description(OffenderDocumentDetail.Type.COURT_REPORT_DOCUMENT.getDescription())
                        .build())
                .build();
    }

    private OffenderDocumentDetail offenderDocumentDetailOf(AddressAssessmentDocument document) {
        return offenderDocumentDetailBuilderOf(document)
                .extendedDescription(String.format(
                        "Address assessment on %s",
                        toHumanReadable(document.getAddressAssessment().getAssessmentDate())
                ))
                .type(KeyValue
                        .builder()
                        .code(OffenderDocumentDetail.Type.ADDRESS_ASSESSMENT_DOCUMENT.name())
                        .description(OffenderDocumentDetail.Type.ADDRESS_ASSESSMENT_DOCUMENT.getDescription())
                        .build())
                .build();
    }

    private OffenderDocumentDetail offenderDocumentDetailOf(ApprovedPremisesReferralDocument document) {
        return offenderDocumentDetailBuilderOf(document)
                .extendedDescription(String.format(
                        "Approved premises referral on %s",
                        toHumanReadable(document.getApprovedPremisesReferral().getReferralDate())
                ))
                .type(KeyValue
                        .builder()
                        .code(OffenderDocumentDetail.Type.APPROVED_PREMISES_REFERRAL_DOCUMENT.name())
                        .description(OffenderDocumentDetail.Type.APPROVED_PREMISES_REFERRAL_DOCUMENT.getDescription())
                        .build())
                .build();
    }

    private OffenderDocumentDetail offenderDocumentDetailOf(AssessmentDocument document) {
        return offenderDocumentDetailBuilderOf(document)
                .extendedDescription(String.format(
                        "Assessment for %s on %s",
                        document.getAssessment().getAssessmentType().getDescription(),
                        toHumanReadable(document.getAssessment().getAssessmentDate())
                ))
                .type(KeyValue
                        .builder()
                        .code(OffenderDocumentDetail.Type.ASSESSMENT_DOCUMENT.name())
                        .description(OffenderDocumentDetail.Type.ASSESSMENT_DOCUMENT.getDescription())
                        .build())
                .build();
    }

    private OffenderDocumentDetail offenderDocumentDetailOf(CaseAllocationDocument document) {
        return offenderDocumentDetailBuilderOf(document)
                .type(KeyValue
                        .builder()
                        .code(OffenderDocumentDetail.Type.CASE_ALLOCATION_DOCUMENT.name())
                        .description(OffenderDocumentDetail.Type.CASE_ALLOCATION_DOCUMENT.getDescription())
                        .build())
                .build();
    }

    private OffenderDocumentDetail offenderDocumentDetailOf(PersonalContactDocument document) {
        return offenderDocumentDetailBuilderOf(document)
                .extendedDescription(String.format(
                        "Personal contact of type %s with %s",
                        document.getPersonalContact().getRelationshipType().getCodeDescription(),
                        document.getPersonalContact().getRelationship()
                ))
                .type(KeyValue
                        .builder()
                        .code(OffenderDocumentDetail.Type.PERSONAL_CONTACT_DOCUMENT.name())
                        .description(OffenderDocumentDetail.Type.PERSONAL_CONTACT_DOCUMENT.getDescription())
                        .build())
                .build();
    }

    private OffenderDocumentDetail offenderDocumentDetailOf(ReferralDocument document) {
        return offenderDocumentDetailBuilderOf(document)
                .extendedDescription(String.format(
                        "Referral for %s on %s",
                        document.getReferral().getReferralType().getDescription(),
                        toHumanReadable(document.getReferral().getReferralDate())
                ))
                .type(KeyValue
                        .builder()
                        .code(OffenderDocumentDetail.Type.REFERRAL_DOCUMENT.name())
                        .description(OffenderDocumentDetail.Type.REFERRAL_DOCUMENT.getDescription())
                        .build())
                .build();
    }

    private OffenderDocumentDetail offenderDocumentDetailOf(PersonalCircumstanceDocument document) {
        return offenderDocumentDetailBuilderOf(document)
                .extendedDescription(String.format(
                        "Personal circumstance of %s started on %s",
                        document.getPersonalCircumstance().getCircumstanceType().getCodeDescription(),
                        toHumanReadable(document.getPersonalCircumstance().getStartDate())
                ))
                .type(KeyValue
                        .builder()
                        .code(OffenderDocumentDetail.Type.PERSONAL_CIRCUMSTANCE_DOCUMENT.name())
                        .description(OffenderDocumentDetail.Type.PERSONAL_CIRCUMSTANCE_DOCUMENT.getDescription())
                        .build())
                .build();
    }

    private OffenderDocumentDetail offenderDocumentDetailOf(UPWAppointmentDocument document) {
        return offenderDocumentDetailBuilderOf(document)
                .type(KeyValue
                        .builder()
                        .code(OffenderDocumentDetail.Type.UPW_APPOINTMENT_DOCUMENT.name())
                        .description(OffenderDocumentDetail.Type.UPW_APPOINTMENT_DOCUMENT.getDescription())
                        .build())
                .extendedDescription(String.format(
                        "Unpaid work appointment on %s for %s",
                        toHumanReadable(document.getUpwAppointment().getAppointmentDate()),
                        document.getUpwAppointment().getUpwProject().getName()))
                        .build();
    }

    private OffenderDocumentDetail offenderDocumentDetailOf(ContactDocument document) {
        return offenderDocumentDetailBuilderOf(document)
                .type(KeyValue
                        .builder()
                        .code(OffenderDocumentDetail.Type.CONTACT_DOCUMENT.name())
                        .description(OffenderDocumentDetail.Type.CONTACT_DOCUMENT.getDescription())
                        .build())
                .extendedDescription(String.format(
                        "Contact on %s for %s",
                        toHumanReadable(document.getContact().getContactDate()),
                        document.getContact().getContactType().getDescription()))
                        .build();
    }


    private String toHumanReadable(LocalDateTime maybeDate) {
        return Optional
                .ofNullable(maybeDate)
                .map(date -> date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .orElse("");
    }
    private String toHumanReadable(LocalDate maybeDate) {
        return Optional
                .ofNullable(maybeDate)
                .map(date -> date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .orElse("");
    }

    private OffenderDocumentDetail offenderDocumentDetailOf(InstitutionalReportDocument document) {
        return offenderDocumentDetailBuilderOf(document)
                .extendedDescription(String.format(
                        "%s at %s requested on %s",
                        document.getInstitutionalReport().getInstitutionalReportType().getCodeDescription(),
                        document.getInstitutionalReport().getInstitution().getInstitutionName(),
                        toHumanReadable(document.getInstitutionalReport().getDateRequested())
                ))
                .type(KeyValue
                        .builder()
                        .code(OffenderDocumentDetail.Type.INSTITUTION_REPORT_DOCUMENT.name())
                        .description(OffenderDocumentDetail.Type.INSTITUTION_REPORT_DOCUMENT.getDescription())
                        .build())
                .build();
    }

    private OffenderDocumentDetail offenderDocumentDetailOf(NsiDocument document) {
        return offenderDocumentDetailBuilderOf(document)
                .extendedDescription(String.format(
                        "Non Statutory Intervention for %s on %s",
                        document.getNsi().getNsiType().getDescription(),
                        toHumanReadable(document.getNsi().getReferralDate())
                ))
                .type(KeyValue
                        .builder()
                        .code(OffenderDocumentDetail.Type.NSI_DOCUMENT.name())
                        .description(OffenderDocumentDetail.Type.NSI_DOCUMENT.getDescription())
                        .build())
                .build();
    }

    private OffenderDocumentDetail.OffenderDocumentDetailBuilder offenderDocumentDetailBuilderOf(Document document) {
        return OffenderDocumentDetail
                .builder()
                .author(authorOf(document))
                .createdAt(createAtOf(document))
                .documentName(document.getDocumentName())
                .id(document.getAlfrescoId())
                .lastModifiedAt(document.getLastSaved());
    }

    private String authorOf(Document document) {
        return Optional
                .ofNullable(document.getCreatedByUser()) // this can be null since bug in document service is not setting this !
                .map(this::fullName)
                .orElse(
                    Optional
                        .ofNullable(document.getLastUpdatedByUser())
                        .map(this::fullName)
                        .orElse(null));



    }

    private LocalDateTime createAtOf(Document document) {
        return Optional
                .ofNullable(document.getCreatedDate())
                .orElseGet(document::getLastSaved);
    }

    private String fullName(User user) {
        return String.format("%s %s", user.getForename(), user.getSurname());
    }
}
