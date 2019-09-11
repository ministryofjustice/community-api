package uk.gov.justice.digital.delius.util;

import uk.gov.justice.digital.delius.jpa.standard.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static uk.gov.justice.digital.delius.util.OffenderHelper.anOffender;

public class EntityHelper {
    public static InstitutionalReportDocument anInstitutionalReportDocument() {
        final InstitutionalReportDocument document = new InstitutionalReportDocument();
        populateBasics(document);
        document.setInstitutionalReport(anInstitutionalReport());
        return document;
    }

    public static InstitutionalReportDocument anInstitutionalReportDocument(Long eventId) {
        final InstitutionalReportDocument document = anInstitutionalReportDocument();
        document.getInstitutionalReport().getCustody().getDisposal().getEvent().setEventId(eventId);
        return document;
    }

    public static InstitutionalReport anInstitutionalReport() {
        return InstitutionalReport
                .builder()
                .custody(Custody
                        .builder()
                        .disposal(Disposal
                                .builder()
                                .event(anEvent())
                                .build())
                        .build())
                .dateRequested(LocalDateTime.now())
                .institutionalReportType(StandardReference
                        .builder()
                        .codeDescription("PAROM1")
                        .build())
                .institution(RInstitution
                        .builder()
                        .institutionName("Windsor Prison")
                        .build())
                .build();
    }

    public static Offender anOffenderWithPreviousConvictionsDocument() {
        final Offender offender = anOffender();
        offender.setPrevConvictionDocumentName("precons.pdf");
        offender.setPreviousConvictionDate(LocalDate.now());
        offender.setPreviousConvictionsAlfrescoDocumentId("123");
        offender.setPreviousConvictionsCreatedByUser(User
                .builder()
                .forename("createdforename")
                .surname("createdsurname")
                .build());
        offender.setPreviousConvictionsCreatedDatetime(LocalDateTime.now());
        return offender;
    }

    public static Event anEvent() {
        return Event
                .builder()
                .eventId(100L)
                .cpsAlfrescoDocumentId("123")
                .cpsCreatedByUser(User
                        .builder()
                        .forename("createdforename")
                        .surname("createdsurname")
                        .build())
                .cpsDocumentName("cps.pdf")
                .cpsDate(LocalDate.now())
                .cpsCreatedDatetime(LocalDateTime.now())
                .cpsSoftDeleted(0L)
                .build();
    }

    public static OffenderDocument anOffenderDocument() {
        final OffenderDocument offenderDocument = new OffenderDocument();

        populateBasics(offenderDocument);
        return offenderDocument;
    }

    public static CourtReportDocument aCourtReportDocument() {
        final CourtReportDocument document = new CourtReportDocument();
        populateBasics(document);
        document.setCourtReport(aCourtReport());
        return document;
    }

    public static CourtReportDocument aCourtReportDocument(Long eventId) {
        final CourtReportDocument document = aCourtReportDocument();
        document.getCourtReport().getCourtAppearance().getEvent().setEventId(eventId);
        return document;
    }

    public static CourtReport aCourtReport() {
        return CourtReport
                .builder()
                .dateRequested(LocalDateTime.now())
                .courtReportType(RCourtReportType
                        .builder()
                        .description("Pre Sentence Report")
                        .build())
                .courtAppearance(CourtAppearance
                        .builder()
                        .court(Court
                                .builder()
                                .courtName("Sheffield Magistrates Court")
                                .build())
                        .event(Event
                                .builder()
                                .eventId(1L)
                                .build())
                        .build())
                .build();
    }

    public static void populateBasics(Document document) {
        document.setAlfrescoId("123");
        document.setCreatedByProbationAreaId(1L);
        document.setCreatedByUser(User
                .builder()
                .forename("createdforename")
                .surname("createdsurname")
                .build());
        document.setCreatedDate(LocalDateTime.now());
        document.setLastUpdatedByUser(User
                .builder()
                .forename("updatedforename")
                .surname("updatedsurname")
                .build());
        document.setDocumentName("DocumentName.pdf");
        document.setLastSaved(LocalDateTime.now());

    }

    public static EventDocument anEventDocument(Long eventId) {
        final EventDocument document = new EventDocument();
        populateBasics(document);
        Event event = anEvent();
        event.setEventId(eventId);
        document.setEvent(event);
        return document;
    }



}
