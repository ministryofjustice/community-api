package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.OffenderDocuments;
import uk.gov.justice.digital.delius.data.filters.DocumentFilter;
import uk.gov.justice.digital.delius.jpa.national.repository.DocumentRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtReportDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReportDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.aCaseAllocationDocument;
import static uk.gov.justice.digital.delius.util.EntityHelper.aContactDocument;
import static uk.gov.justice.digital.delius.util.EntityHelper.aCourtReportDocument;
import static uk.gov.justice.digital.delius.util.EntityHelper.aNsiDocument;
import static uk.gov.justice.digital.delius.util.EntityHelper.aPersonalCircumstanceDocument;
import static uk.gov.justice.digital.delius.util.EntityHelper.aPersonalContactDocument;
import static uk.gov.justice.digital.delius.util.EntityHelper.aReferralDocument;
import static uk.gov.justice.digital.delius.util.EntityHelper.aUPWAppointmentDocument;
import static uk.gov.justice.digital.delius.util.EntityHelper.anAddressAssessmentDocument;
import static uk.gov.justice.digital.delius.util.EntityHelper.anApprovedPremisesReferralDocument;
import static uk.gov.justice.digital.delius.util.EntityHelper.anAssessmentDocument;
import static uk.gov.justice.digital.delius.util.EntityHelper.anEvent;
import static uk.gov.justice.digital.delius.util.EntityHelper.anEventDocument;
import static uk.gov.justice.digital.delius.util.EntityHelper.anInstitutionalReportDocument;
import static uk.gov.justice.digital.delius.util.OffenderHelper.anOffender;

@ExtendWith(MockitoExtension.class)
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
    @Mock
    private AddressAssessmentDocumentRepository addressAssessmentRepository;
    @Mock
    private ApprovedPremisesReferralDocumentRepository approvedPremisesReferralDocumentRepository;
    @Mock
    private AssessmentDocumentRepository assessmentDocumentRepository;
    @Mock
    private CaseAllocationDocumentRepository caseAllocationDocumentRepository;
    @Mock
    private PersonalContactDocumentRepository personalContactDocumentRepository;
    @Mock
    private ReferralDocumentRepository referralDocumentRepository;
    @Mock
    private NsiDocumentRepository nsiDocumentRepository;
    @Mock
    private PersonalCircumstanceDocumentRepository personalCircumstanceDocumentRepository;
    @Mock
    private UPWAppointmentDocumentRepository upwAppointmentDocumentRepository;
    @Mock
    private ContactDocumentRepository contactDocumentRepository;


    @BeforeEach
    public void before() {
        documentService = new DocumentService(
                documentRepository,
                offenderRepository,
                offenderDocumentRepository,
                eventDocumentRepository,
                courtReportDocumentRepository,
                institutionReportDocumentRepository,
                eventRepository,
                addressAssessmentRepository,
                approvedPremisesReferralDocumentRepository,
                assessmentDocumentRepository,
                caseAllocationDocumentRepository,
                personalContactDocumentRepository,
                referralDocumentRepository,
                nsiDocumentRepository,
                personalCircumstanceDocumentRepository,
                upwAppointmentDocumentRepository,
                contactDocumentRepository
        );
        when(offenderRepository.findByOffenderId(any())).thenReturn(Optional.of(anOffender()));
        when(offenderDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of());
        when(eventDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of());
        when(courtReportDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of());
        when(institutionReportDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of());
        when(eventRepository.findByOffenderId(any())).thenReturn(ImmutableList.of());
        when(addressAssessmentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of());
        when(approvedPremisesReferralDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of());
        when(assessmentDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of());
        when(personalContactDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of());
        when(referralDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of());
        when(nsiDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of());
        when(personalCircumstanceDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of());
        when(upwAppointmentDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of());
        when(contactDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of());
    }

    @Test
    public void noConvictionsAddedWhenNoEventTypeDocuments() {
        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getConvictions()).isEmpty();
    }

    @Test
    public void singleConvictionAddedWhenSingleCourtReport() {
        when(courtReportDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(aCourtReportDocument()));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

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

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

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

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getConvictions()).hasSize(2);
    }

    @Test
    public void singleConvictionAddedWhenSingleInstitutionalReport() {
        when(institutionReportDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(anInstitutionalReportDocument()));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

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

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

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

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getConvictions()).hasSize(2);
    }

    @Test
    public void noConvictionsAddedWhenSingleEventHasNoCPSPack() {
        final Event event = anEvent();
        event.setCpsAlfrescoDocumentId(null);

        when(eventRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(event));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getConvictions()).hasSize(0);
    }

    @Test
    public void singleConvictionsAddedWhenSingleEventHasCPSPack() {
        final Event event = anEvent();
        event.setCpsAlfrescoDocumentId("123");

        when(eventRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(event));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getConvictions()).hasSize(1);
    }

    @Test
    public void singleOffenderDocumentAddedWhenOffenderHasPreviousConvictionsUploaded() {
        final Offender offender = anOffender();
        offender.setPreviousConvictionsAlfrescoDocumentId("123");

        when(offenderRepository.findByOffenderId(any())).thenReturn(Optional.of(offender));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getDocuments()).hasSize(1);
    }

    @Test
    public void noOffenderDocumentsAddedWhenOffenderHasNoPreviousConvictionsUploaded() {
        final Offender offender = anOffender();
        offender.setPreviousConvictionsAlfrescoDocumentId(null);

        when(offenderRepository.findByOffenderId(any())).thenReturn(Optional.of(offender));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

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

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

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

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

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

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getConvictions()).hasSize(2);
        assertThat(documents.getConvictions().get(0).getDocuments()).hasSize(1);
        assertThat(documents.getConvictions().get(1).getDocuments()).hasSize(3);
    }

    @Test
    public void addressAssessmentDocumentsAddedToOffenderDocuments() {
        when(addressAssessmentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(
                anAddressAssessmentDocument(),
                anAddressAssessmentDocument()
        ));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getDocuments()).hasSize(2);
    }

    @Test
    public void approvedPremisesReferralDocumentsDistributedToEachConviction() {
        when(approvedPremisesReferralDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(
                anApprovedPremisesReferralDocument(1L),
                anApprovedPremisesReferralDocument(2L),
                anApprovedPremisesReferralDocument(2L),
                anApprovedPremisesReferralDocument(2L)
        ));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getConvictions()).hasSize(2);
        assertThat(documents.getConvictions().get(0).getDocuments()).hasSize(1);
        assertThat(documents.getConvictions().get(1).getDocuments()).hasSize(3);
    }

    @Test
    public void assessmentDocumentsDistributedToEachConviction() {
        when(assessmentDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(
                anAssessmentDocument(1L),
                anAssessmentDocument(2L),
                anAssessmentDocument(2L),
                anAssessmentDocument(2L)
        ));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getConvictions()).hasSize(2);
        assertThat(documents.getConvictions().get(0).getDocuments()).hasSize(1);
        assertThat(documents.getConvictions().get(1).getDocuments()).hasSize(3);
    }

    @Test
    public void caseAllocationDocumentsDistributedToEachConviction() {
        when(caseAllocationDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(
                aCaseAllocationDocument(1L),
                aCaseAllocationDocument(2L),
                aCaseAllocationDocument(2L),
                aCaseAllocationDocument(2L)
        ));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getConvictions()).hasSize(2);
        assertThat(documents.getConvictions().get(0).getDocuments()).hasSize(1);
        assertThat(documents.getConvictions().get(1).getDocuments()).hasSize(3);
    }

    @Test
    public void personalContactDocumentsAddedToOffenderDocuments() {
        when(personalContactDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(
                aPersonalContactDocument(),
                aPersonalContactDocument()
        ));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getDocuments()).hasSize(2);
    }

    @Test
    public void referralDocumentsDistributedToEachConviction() {
        when(referralDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(
                aReferralDocument(1L),
                aReferralDocument(2L),
                aReferralDocument(2L),
                aReferralDocument(2L)
        ));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getConvictions()).hasSize(2);
        assertThat(documents.getConvictions().get(0).getDocuments()).hasSize(1);
        assertThat(documents.getConvictions().get(1).getDocuments()).hasSize(3);
    }

    @Test
    public void nsiDocumentsDistributedToEachConvictionAndOffender() {
        when(nsiDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(
                aNsiDocument(1L),
                aNsiDocument(2L),
                aNsiDocument(2L),
                aNsiDocument(2L),
                aNsiDocument(),
                aNsiDocument()
        ));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getConvictions()).hasSize(2);
        assertThat(documents.getConvictions().get(0).getDocuments()).hasSize(1);
        assertThat(documents.getConvictions().get(1).getDocuments()).hasSize(3);
        assertThat(documents.getDocuments()).hasSize(2);
    }

    @Test
    public void personalCircumstanceDocumentsAddedToOffenderDocuments() {
        when(personalCircumstanceDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(
                aPersonalCircumstanceDocument(),
                aPersonalCircumstanceDocument()
        ));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getDocuments()).hasSize(2);
    }

    @Test
    public void upwAppointmentDocumentsDistributedToEachConviction() {
        when(upwAppointmentDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(
                aUPWAppointmentDocument(1L),
                aUPWAppointmentDocument(2L),
                aUPWAppointmentDocument(2L),
                aUPWAppointmentDocument(2L)
        ));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getConvictions()).hasSize(2);
        assertThat(documents.getConvictions().get(0).getDocuments()).hasSize(1);
        assertThat(documents.getConvictions().get(1).getDocuments()).hasSize(3);
    }

    @Test
    public void contactDocumentsDistributedToEachConvictionAndOffender() {
        when(contactDocumentRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(
                aContactDocument(1L),
                aContactDocument(2L),
                aContactDocument(2L),
                aContactDocument(2L),
                aContactDocument(),
                aContactDocument()
        ));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getConvictions()).hasSize(2);
        assertThat(documents.getConvictions().get(0).getDocuments()).hasSize(1);
        assertThat(documents.getConvictions().get(1).getDocuments()).hasSize(3);
        assertThat(documents.getDocuments()).hasSize(2);
    }

}
