package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.OffenderDocumentDetail;
import uk.gov.justice.digital.delius.data.api.OffenderDocumentDetail.Type;
import uk.gov.justice.digital.delius.data.api.ReportDocumentDates;
import uk.gov.justice.digital.delius.jpa.standard.entity.AddressAssessmentDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.ApprovedPremisesReferralDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.AssessmentDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.CaseAllocationDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtReportDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.Document;
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
import uk.gov.justice.digital.delius.jpa.standard.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class DocumentTransformer {
    public static List<OffenderDocumentDetail> offenderDocumentsDetailsOfOffenderDocuments(List<OffenderDocument> documents) {
        return documents
            .stream()
            .map(DocumentTransformer::offenderDocumentDetailOf)
            .collect(toList());
    }

    public static List<OffenderDocumentDetail> offenderDocumentsDetailsOfEventDocuments(List<EventDocument> documents) {
        return documents
            .stream()
            .map(DocumentTransformer::offenderDocumentDetailOf)
            .collect(toList());
    }

    public static List<OffenderDocumentDetail> offenderDocumentsDetailsOfCourtReportDocuments(List<CourtReportDocument> documents) {
        return documents
            .stream()
            .map(DocumentTransformer::offenderDocumentDetailOf)
            .collect(toList());
    }

    public static List<OffenderDocumentDetail> offenderDocumentsDetailsOfInstitutionReportDocuments(List<InstitutionalReportDocument> documents) {
        return documents
            .stream()
            .map(DocumentTransformer::offenderDocumentDetailOf)
            .collect(toList());
    }

    public static List<OffenderDocumentDetail> offenderDocumentsDetailsOfAddressAssessmentDocuments(List<AddressAssessmentDocument> documents) {
        return documents
            .stream()
            .map(DocumentTransformer::offenderDocumentDetailOf)
            .collect(toList());
    }

    public static List<OffenderDocumentDetail> offenderDocumentsDetailsOfApprovedPremisesReferralDocuments(List<ApprovedPremisesReferralDocument> documents) {
        return documents
            .stream()
            .map(DocumentTransformer::offenderDocumentDetailOf)
            .collect(toList());
    }

    public static List<OffenderDocumentDetail> offenderDocumentsDetailsOfAssessmentDocuments(List<AssessmentDocument> documents) {
        return documents
            .stream()
            .map(DocumentTransformer::offenderDocumentDetailOf)
            .collect(toList());
    }

    public static List<OffenderDocumentDetail> offenderDocumentsDetailsOfCaseAllocationDocuments(List<CaseAllocationDocument> documents) {
        return documents
            .stream()
            .map(DocumentTransformer::offenderDocumentDetailOf)
            .collect(toList());
    }


    public static List<OffenderDocumentDetail> offenderDocumentsDetailsOfPersonalContactDocuments(List<PersonalContactDocument> documents) {
        return documents
            .stream()
            .map(DocumentTransformer::offenderDocumentDetailOf)
            .collect(toList());
    }

    public static List<OffenderDocumentDetail> offenderDocumentsDetailsOfReferralDocuments(List<ReferralDocument> documents) {
        return documents
            .stream()
            .map(DocumentTransformer::offenderDocumentDetailOf)
            .collect(toList());
    }


    public static List<OffenderDocumentDetail> offenderDocumentsDetailsOfNsiDocuments(List<NsiDocument> documents) {
        return documents
            .stream()
            .map(DocumentTransformer::offenderDocumentDetailOf)
            .collect(toList());
    }


    public static List<OffenderDocumentDetail> offenderDocumentsDetailsOfPersonalCircumstanceDocuments(List<PersonalCircumstanceDocument> documents) {
        return documents
            .stream()
            .map(DocumentTransformer::offenderDocumentDetailOf)
            .collect(toList());
    }


    public static List<OffenderDocumentDetail> offenderDocumentsDetailsOfUPWAppointmentDocuments(List<UPWAppointmentDocument> documents) {
        return documents
            .stream()
            .map(DocumentTransformer::offenderDocumentDetailOf)
            .collect(toList());
    }


    public static List<OffenderDocumentDetail> offenderDocumentsDetailsOfContactDocuments(List<ContactDocument> documents) {
        return documents
            .stream()
            .map(DocumentTransformer::offenderDocumentDetailOf)
            .collect(toList());
    }

    private static OffenderDocumentDetail offenderDocumentDetailOf(OffenderDocument document) {
        OffenderDocumentDetail.Type type = document.getDocumentType() == DocumentType.PREVIOUS_CONVICTION ?
            Type.PRECONS_DOCUMENT : Type.OFFENDER_DOCUMENT;
        return offenderDocumentDetailBuilderOf(document)
            .type(KeyValue
                .builder()
                .code(type.name())
                .description(type.getDescription())
                .build())
            .build();
    }

    private static OffenderDocumentDetail offenderDocumentDetailOf(EventDocument document) {
        OffenderDocumentDetail.Type type = document.getDocumentType() == DocumentType.CPS_PACK ?
            Type.CPSPACK_DOCUMENT : Type.CONVICTION_DOCUMENT;
        return offenderDocumentDetailBuilderOf(document)
            .type(KeyValue
                .builder()
                .code(type.name())
                .description(type.getDescription())
                .build())
            .build();
    }

    private static OffenderDocumentDetail offenderDocumentDetailOf(CourtReportDocument document) {
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
            .subType(KeyValue
                .builder()
                .code(document.getCourtReport().getCourtReportType().getCode())
                .description(document.getCourtReport().getCourtReportType().getDescription())
                .build())
            .reportDocumentDates(ReportDocumentDates.builder()
                .requestedDate(Optional.ofNullable(document.getCourtReport().getDateRequested())
                    .map(LocalDateTime::toLocalDate)
                    .orElse(null))
                .requiredDate(Optional.ofNullable(document.getCourtReport().getDateRequired())
                    .map(LocalDateTime::toLocalDate)
                    .orElse(null))
                .completedDate(document.getCourtReport().getCompletedDate())
                .build())
            .build();
    }

    private static OffenderDocumentDetail offenderDocumentDetailOf(AddressAssessmentDocument document) {
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

    private static OffenderDocumentDetail offenderDocumentDetailOf(ApprovedPremisesReferralDocument document) {
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

    private static OffenderDocumentDetail offenderDocumentDetailOf(AssessmentDocument document) {
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

    private static OffenderDocumentDetail offenderDocumentDetailOf(CaseAllocationDocument document) {
        return offenderDocumentDetailBuilderOf(document)
            .type(KeyValue
                .builder()
                .code(OffenderDocumentDetail.Type.CASE_ALLOCATION_DOCUMENT.name())
                .description(OffenderDocumentDetail.Type.CASE_ALLOCATION_DOCUMENT.getDescription())
                .build())
            .build();
    }

    private static OffenderDocumentDetail offenderDocumentDetailOf(PersonalContactDocument document) {
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

    private static OffenderDocumentDetail offenderDocumentDetailOf(ReferralDocument document) {
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

    private static OffenderDocumentDetail offenderDocumentDetailOf(PersonalCircumstanceDocument document) {
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

    private static OffenderDocumentDetail offenderDocumentDetailOf(UPWAppointmentDocument document) {
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

    private static OffenderDocumentDetail offenderDocumentDetailOf(ContactDocument document) {
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


    private static String toHumanReadable(LocalDateTime maybeDate) {
        return Optional
            .ofNullable(maybeDate)
            .map(date -> date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            .orElse("");
    }

    private static String toHumanReadable(LocalDate maybeDate) {
        return Optional
            .ofNullable(maybeDate)
            .map(date -> date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            .orElse("");
    }

    private static OffenderDocumentDetail offenderDocumentDetailOf(InstitutionalReportDocument document) {
        return offenderDocumentDetailBuilderOf(document)
            .extendedDescription(String.format(
                "%s at %s requested on %s",
                document.getInstitutionalReport().getInstitutionalReportType().getCodeDescription(),
                document.getInstitutionalReport().getInstitution().getInstitutionName(),
                DocumentTransformer.toHumanReadable(document.getInstitutionalReport().getDateRequested())
            ))
            .type(KeyValue
                .builder()
                .code(OffenderDocumentDetail.Type.INSTITUTION_REPORT_DOCUMENT.name())
                .description(OffenderDocumentDetail.Type.INSTITUTION_REPORT_DOCUMENT.getDescription())
                .build())
            .subType(KeyValue
                .builder()
                .code(document.getInstitutionalReport().getInstitutionalReportType().getCodeValue())
                .description(document.getInstitutionalReport().getInstitutionalReportType().getCodeDescription())
                .build())
            .reportDocumentDates(ReportDocumentDates.builder()
                .requestedDate(document.getInstitutionalReport().getDateRequested())
                .requiredDate(document.getInstitutionalReport().getDateRequired())
                .completedDate(Optional.ofNullable(document.getInstitutionalReport().getDateCompleted()).map(LocalDate::atStartOfDay).orElse(null))
                .build())
            .build();
    }

    private static OffenderDocumentDetail offenderDocumentDetailOf(NsiDocument document) {
        return offenderDocumentDetailBuilderOf(document)
            .extendedDescription(String.format(
                "Non Statutory Intervention for %s on %s",
                document.getNsi().getNsiType().getDescription(),
                DocumentTransformer.toHumanReadable(document.getNsi().getReferralDate())
            ))
            .type(KeyValue
                .builder()
                .code(OffenderDocumentDetail.Type.NSI_DOCUMENT.name())
                .description(OffenderDocumentDetail.Type.NSI_DOCUMENT.getDescription())
                .build())
            .build();
    }

    private static OffenderDocumentDetail.OffenderDocumentDetailBuilder offenderDocumentDetailBuilderOf(Document document) {
        return OffenderDocumentDetail
            .builder()
            .author(authorOf(document))
            .createdAt(createAtOf(document))
            .documentName(document.getDocumentName())
            .id(document.getAlfrescoId())
            .parentPrimaryKeyId(document.getPrimaryKeyId())
            .lastModifiedAt(document.getLastSaved());
    }

    private static String authorOf(Document document) {
        return Optional
            .ofNullable(document.getCreatedByUser()) // this can be null since bug in document service is not setting this !
            .map(DocumentTransformer::fullName)
            .orElse(
                Optional
                    .ofNullable(document.getLastUpdatedByUser())
                    .map(DocumentTransformer::fullName)
                    .orElse(null));


    }

    private static LocalDateTime createAtOf(Document document) {
        return Optional
            .ofNullable(document.getCreatedDate())
            .orElseGet(document::getLastSaved);
    }

    private static String fullName(User user) {
        return String.format("%s %s", user.getForename(), user.getSurname());
    }
}
