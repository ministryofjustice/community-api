package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import uk.gov.justice.digital.delius.data.api.OffenderDocumentDetail;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.util.EntityHelper.*;

public class DocumentTransformerTest {
    private DocumentTransformer documentTransformer;

    @Before
    public void before() {
        documentTransformer = new DocumentTransformer();
    }

    @Test
    public void authorNameUsesCreatedByWhenPresent() {
        final OffenderDocument offenderDocument = anOffenderDocument();
        offenderDocument.setCreatedByUser(
                User
                        .builder()
                        .forename("Bobby")
                        .surname("Bread")
                        .build()
        );
        offenderDocument.setLastUpdatedByUser(
                User
                        .builder()
                        .forename("Lardy")
                        .surname("Lump")
                        .build()
        );

        assertThat(documentTransformer.offenderDocumentsDetailsOfOffenderDocuments(ImmutableList.of(offenderDocument))).hasSize(1);
        assertThat(documentTransformer.offenderDocumentsDetailsOfOffenderDocuments(ImmutableList.of(offenderDocument)).get(0).getAuthor()).isEqualTo("Bobby Bread");
    }
    @Test
    public void authorNameUsesLastUpdatedByWhenCreatedByNotPresent() {
        final OffenderDocument offenderDocument = anOffenderDocument();
        offenderDocument.setCreatedByUser(null);
        offenderDocument.setLastUpdatedByUser(
                User
                        .builder()
                        .forename("Lardy")
                        .surname("Lump")
                        .build()
        );

        assertThat(documentTransformer.offenderDocumentsDetailsOfOffenderDocuments(ImmutableList.of(offenderDocument))).hasSize(1);
        assertThat(documentTransformer.offenderDocumentsDetailsOfOffenderDocuments(ImmutableList.of(offenderDocument))
                .get(0).getAuthor()).isEqualTo("Lardy Lump");
    }

    @Test
    public void authorNameUsesEventCPSCreatedByWhenPresent() {
        final Event event = anEvent();
        event.setCpsCreatedByUser(
                User
                        .builder()
                        .forename("Bobby")
                        .surname("Bread")
                        .build()
        );

        assertThat(documentTransformer.offenderDocumentDetailsOfCpsPack(event)
                .getAuthor()).isEqualTo("Bobby Bread");
    }
    @Test
    public void authorLeftNullForEventCPSCreatedByWhenNotPresent() {
        final Event event = anEvent();
        event.setCpsCreatedByUser(null);

        assertThat(documentTransformer.offenderDocumentDetailsOfCpsPack(event)
                .getAuthor()).isNull();
    }

    @Test
    public void authorNameUsesOffenderPreviousConvictionsCreatedByWhenPresent() {
        final Offender offender = anOffenderWithPreviousConvictionsDocument();
        offender.setPreviousConvictionsCreatedByUser(
                User
                        .builder()
                        .forename("Bobby")
                        .surname("Bread")
                        .build()
        );

        assertThat(documentTransformer.offenderDocumentDetailsOfPreviousConvictions(offender)
                .getAuthor()).isEqualTo("Bobby Bread");
    }

    @Test
    public void authorNameLeftNullWhenOffenderPreviousConvictionsCreatedByWhenNotPresent() {
        final Offender offender = anOffenderWithPreviousConvictionsDocument();
        offender.setPreviousConvictionsCreatedByUser(null);

        assertThat(documentTransformer.offenderDocumentDetailsOfPreviousConvictions(offender)
                .getAuthor()).isNull();
    }

    @Test
    public void cpsPackDescriptionsSet() {
        final Event event = anEvent();
        event.setCpsDate(LocalDate.of(1965, 7, 19));

        final OffenderDocumentDetail offenderDocumentDetail = documentTransformer.offenderDocumentDetailsOfCpsPack(event);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("CPSPACK_DOCUMENT");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Crown Prosecution Service case pack");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Crown Prosecution Service case pack for 19/07/1965");
    }

    @Test
    public void previousConvictionsDescriptionsSet() {
        final Offender offender = anOffenderWithPreviousConvictionsDocument();
        offender.setPreviousConvictionDate(LocalDate.of(1965, 7, 19));

        final OffenderDocumentDetail offenderDocumentDetail = documentTransformer.offenderDocumentDetailsOfPreviousConvictions(offender);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("PRECONS_DOCUMENT");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("PNC previous convictions");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Previous convictions as of 19/07/1965");
    }

    @Test
    public void courtReportDescriptionsSet() {
        final CourtReportDocument document = aCourtReportDocument();

        document.getCourtReport().getCourtReportType().setDescription("Pre Sentence Report");
        document.getCourtReport().setDateRequested(LocalDateTime.of(1965, 7, 19, 0, 0));
        document.getCourtReport().getCourtAppearance().getCourt().setCourtName("Sheffield Crown Court");

        assertThat(documentTransformer.offenderDocumentsDetailsOfCourtReportDocuments(ImmutableList.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = documentTransformer.offenderDocumentsDetailsOfCourtReportDocuments(ImmutableList.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("COURT_REPORT_DOCUMENT");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Court report");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Pre Sentence Report requested by Sheffield Crown Court on 19/07/1965");

    }

    @Test
    public void institutionReportDescriptionsSet() {
        final InstitutionalReportDocument document = anInstitutionalReportDocument();

        document.getInstitutionalReport().getInstitutionalReportType().setCodeDescription("Parole");
        document.getInstitutionalReport().setDateRequested(LocalDateTime.of(1965, 7, 19, 0, 0));
        document.getInstitutionalReport().getInstitution().setInstitutionName("Sheffield jail");

        assertThat(documentTransformer.offenderDocumentsDetailsOfInstitutionReportDocuments(ImmutableList.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = documentTransformer.offenderDocumentsDetailsOfInstitutionReportDocuments(ImmutableList.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("INSTITUTION_REPORT_DOCUMENT");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Institution report");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Parole at Sheffield jail requested on 19/07/1965");
    }


}