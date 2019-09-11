package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.data.api.OffenderDocuments;
import uk.gov.justice.digital.delius.jpa.national.repository.DocumentRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtReportDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReportDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.repository.*;
import uk.gov.justice.digital.delius.transformers.DocumentTransformer;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.*;
import static uk.gov.justice.digital.delius.util.OffenderHelper.anOffender;

@RunWith(MockitoJUnitRunner.class)
public class DocumentServiceTest {
    private DocumentService documentService;

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private OffenderRepository offenderRepository;
    @Mock
    private OffenderDocumentRepository offenderDocumentRepository;
    @Mock
    private EventDocumentRepository eventDocumentRepository;
    @Mock
    private CourtReportDocumentRepository courtReportDocumentRepository;
    @Mock
    private InstitutionReportDocumentRepository institutionReportDocumentRepository;
    @Mock
    private EventRepository eventRepository;

    @Before
    public void before() {
        documentService = new DocumentService(documentRepository, offenderRepository, offenderDocumentRepository, eventDocumentRepository, courtReportDocumentRepository, institutionReportDocumentRepository, eventRepository, new DocumentTransformer());
        when(offenderRepository.findByOffenderId(any())).thenReturn(Optional.of(anOffender()));
        when(offenderDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of());
        when(eventDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of());
        when(courtReportDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of());
        when(institutionReportDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of());
        when(eventRepository.findByOffenderId(any())).thenReturn(ImmutableList.of());
    }

    @Test
    public void noConvictionsAddedWhenNoEventTypeDocuments() {
        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L);

        assertThat(documents.getConvictions()).isEmpty();
    }

    @Test
    public void singleConvictionAddedWhenSingleCourtReport() {
        when(courtReportDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(aCourtReportDocument()));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L);

        assertThat(documents.getConvictions()).hasSize(1);
    }

    @Test
    public void singleConvictionAddedWhenSingleCourtReportAndSingleEventShareSameId() {
        final CourtReportDocument courtReportDocument = aCourtReportDocument();
        courtReportDocument.getCourtReport().getCourtAppearance().getEvent().setEventId(99L);
        final Event event = anEvent();
        event.setEventId(99L);

        when(eventRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(event));
        when(courtReportDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(courtReportDocument));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L);

        assertThat(documents.getConvictions()).hasSize(1);
    }

    @Test
    public void twoConvictionsAddedWhenSingleCourtReportAndSingleEventHaveDifferentId() {
        final CourtReportDocument courtReportDocument = aCourtReportDocument();
        courtReportDocument.getCourtReport().getCourtAppearance().getEvent().setEventId(1L);
        final Event event = anEvent();
        event.setEventId(99L);

        when(eventRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(event));
        when(courtReportDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(courtReportDocument));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L);

        assertThat(documents.getConvictions()).hasSize(2);
    }

    @Test
    public void singleConvictionAddedWhenSingleInstitutionalReport() {
        when(institutionReportDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(anInstitutionalReportDocument()));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L);

        assertThat(documents.getConvictions()).hasSize(1);
    }

    @Test
    public void singleConvictionAddedWhenSingleInstitutionalReportAndSingleEventShareSameId() {
        final InstitutionalReportDocument institutionalReportDocument = anInstitutionalReportDocument();
        institutionalReportDocument.getInstitutionalReport().getCustody().getDisposal().getEvent().setEventId(99L);
        final Event event = anEvent();
        event.setEventId(99L);

        when(eventRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(event));
        when(institutionReportDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(institutionalReportDocument));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L);

        assertThat(documents.getConvictions()).hasSize(1);
    }

    @Test
    public void twoConvictionsAddedWhenSingleInstitutionalReportAndSingleEventHaveDifferentId() {
        final InstitutionalReportDocument institutionalReportDocument = anInstitutionalReportDocument();
        institutionalReportDocument.getInstitutionalReport().getCustody().getDisposal().getEvent().setEventId(1L);
        final Event event = anEvent();
        event.setEventId(99L);

        when(eventRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(event));
        when(institutionReportDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(institutionalReportDocument));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L);

        assertThat(documents.getConvictions()).hasSize(2);
    }

    @Test
    public void noConvictionsAddedWhenSingleEventHasNoCPSPack() {
        final Event event = anEvent();
        event.setCpsAlfrescoDocumentId(null);

        when(eventRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(event));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L);

        assertThat(documents.getConvictions()).hasSize(0);
    }

    @Test
    public void singleConvictionsAddedWhenSingleEventHasCPSPack() {
        final Event event = anEvent();
        event.setCpsAlfrescoDocumentId("123");

        when(eventRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(event));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L);

        assertThat(documents.getConvictions()).hasSize(1);
    }

    @Test
    public void singleOffenderDocumentAddedWhenOffenderHasPreviousConvictionsUploaded() {
        final Offender offender = anOffender();
        offender.setPreviousConvictionsAlfrescoDocumentId("123");

        when(offenderRepository.findByOffenderId(any())).thenReturn(Optional.of(offender));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L);

        assertThat(documents.getDocuments()).hasSize(1);
    }

    @Test
    public void noOffenderDocumentsAddedWhenOffenderHasNoPreviousConvictionsUploaded() {
        final Offender offender = anOffender();
        offender.setPreviousConvictionsAlfrescoDocumentId(null);

        when(offenderRepository.findByOffenderId(any())).thenReturn(Optional.of(offender));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L);

        assertThat(documents.getDocuments()).hasSize(0);
    }

    @Test
    public void eventDocumentsDistributedToEachConviction() {
        when(eventDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(
                anEventDocument(1L),
                anEventDocument(2L),
                anEventDocument(2L),
                anEventDocument(2L)
        ));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L);

        assertThat(documents.getConvictions()).hasSize(2);
        assertThat(documents.getConvictions().get(0).getDocuments()).hasSize(1);
        assertThat(documents.getConvictions().get(1).getDocuments()).hasSize(3);
    }

    @Test
    public void courtReportsDocumentsDistributedToEachConviction() {
        when(courtReportDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(
                aCourtReportDocument(1L),
                aCourtReportDocument(2L),
                aCourtReportDocument(2L),
                aCourtReportDocument(2L)
        ));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L);

        assertThat(documents.getConvictions()).hasSize(2);
        assertThat(documents.getConvictions().get(0).getDocuments()).hasSize(1);
        assertThat(documents.getConvictions().get(1).getDocuments()).hasSize(3);
    }

    @Test
    public void institutionReportDocumentsDistributedToEachConviction() {
        when(institutionReportDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(
                anInstitutionalReportDocument(1L),
                anInstitutionalReportDocument(2L),
                anInstitutionalReportDocument(2L),
                anInstitutionalReportDocument(2L)
        ));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L);

        assertThat(documents.getConvictions()).hasSize(2);
        assertThat(documents.getConvictions().get(0).getDocuments()).hasSize(1);
        assertThat(documents.getConvictions().get(1).getDocuments()).hasSize(3);
    }
}