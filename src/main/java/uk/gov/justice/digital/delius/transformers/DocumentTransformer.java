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
    private OffenderDocumentDetail.OffenderDocumentDetailBuilder offenderDocumentDetailBuilderOf(Document document) {
        return OffenderDocumentDetail
                .builder()
                .author(authorOf(document))
                .createdAt(document.getCreatedDate())
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

    private String fullName(User user) {
        return String.format("%s %s", user.getForename(), user.getSurname());
    }
}
