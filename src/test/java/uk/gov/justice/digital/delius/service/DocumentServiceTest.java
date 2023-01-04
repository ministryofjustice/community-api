package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.digital.delius.data.api.OffenderDocumentDetail;
import uk.gov.justice.digital.delius.data.api.OffenderDocumentDetail.Type;
import uk.gov.justice.digital.delius.data.api.OffenderDocuments;
import uk.gov.justice.digital.delius.data.filters.DocumentFilter;
import uk.gov.justice.digital.delius.jpa.national.repository.DocumentRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtReportDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.Document.DocumentType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.EventDocument;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import static uk.gov.justice.digital.delius.util.EntityHelper.anOffenderDocument;
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
    @Captor
    private ArgumentCaptor<Specification<CourtReportDocument>> courtReportDocumentSpecification;


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
        when(offenderDocumentRepository.findByOffenderId(any(), any())).thenReturn(List.of());
        when(eventDocumentRepository.findByOffenderId(any(), any())).thenReturn(List.of());
        when(courtReportDocumentRepository.findAll(courtReportDocumentSpecification.capture())).thenReturn(List
            .of());
        when(institutionReportDocumentRepository.findByOffenderId(any())).thenReturn(List.of());
        when(eventRepository.findByOffenderId(any())).thenReturn(List.of());
        when(addressAssessmentRepository.findByOffenderId(any())).thenReturn(List.of());
        when(approvedPremisesReferralDocumentRepository.findByOffenderId(any())).thenReturn(List.of());
        when(assessmentDocumentRepository.findByOffenderId(any())).thenReturn(List.of());
        when(personalContactDocumentRepository.findByOffenderId(any())).thenReturn(List.of());
        when(referralDocumentRepository.findByOffenderId(any())).thenReturn(List.of());
        when(nsiDocumentRepository.findByOffenderId(any())).thenReturn(List.of());
        when(personalCircumstanceDocumentRepository.findByOffenderId(any())).thenReturn(List.of());
        when(upwAppointmentDocumentRepository.findByOffenderId(any())).thenReturn(List.of());
        when(contactDocumentRepository.findByOffenderId(any())).thenReturn(List.of());
    }

    @Test
    public void noConvictionsAddedWhenNoEventTypeDocuments() {
        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getConvictions()).isEmpty();
    }

    @Test
    public void singleConvictionAddedWhenSingleCourtReport() {
        when(courtReportDocumentRepository.findAll(courtReportDocumentSpecification.capture())).thenReturn(List.of(aCourtReportDocument()));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getConvictions()).hasSize(1);
    }

    @Test
    public void singleConvictionAddedWhenSingleCourtReportAndSingleEventShareSameId() {
        final CourtReportDocument courtReportDocument = aCourtReportDocument();
        courtReportDocument.getCourtReport().getCourtAppearance().getEvent().setEventId(99L);
        final Event event = anEvent();
        event.setEventId(99L);

        when(eventRepository.findByOffenderId(any())).thenReturn(List.of(event));
        when(courtReportDocumentRepository.findAll(courtReportDocumentSpecification.capture())).thenReturn(List.of(courtReportDocument));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getConvictions()).hasSize(1);
    }

    @Test
    public void twoConvictionsAddedWhenSingleCourtReportAndSingleEventHaveDifferentId() {
        final CourtReportDocument courtReportDocument = aCourtReportDocument();
        courtReportDocument.getCourtReport().getCourtAppearance().getEvent().setEventId(1L);
        final Event event = anEvent();
        event.setEventId(99L);
        final EventDocument cpsPack = anEventDocument(event.getEventId(), DocumentType.CPS_PACK);

        when(eventRepository.findByOffenderId(any())).thenReturn(List.of(event));
        when(courtReportDocumentRepository.findAll(courtReportDocumentSpecification.capture())).thenReturn(List
            .of(courtReportDocument));
        when(eventDocumentRepository.findByOffenderId(any(), eq(DocumentType.CPS_PACK))).thenReturn(List.of(cpsPack));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getConvictions()).hasSize(2);
    }

    @Test
    public void singleConvictionAddedWhenSingleInstitutionalReport() {
        when(institutionReportDocumentRepository.findByOffenderId(any())).thenReturn(List.of(anInstitutionalReportDocument()));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getConvictions()).hasSize(1);
    }

    @Test
    public void singleConvictionAddedWhenSingleInstitutionalReportAndSingleEventShareSameId() {
        final InstitutionalReportDocument institutionalReportDocument = anInstitutionalReportDocument();
        institutionalReportDocument.getInstitutionalReport().getCustody().getDisposal().getEvent().setEventId(99L);
        final Event event = anEvent();
        event.setEventId(99L);

        when(eventRepository.findByOffenderId(any())).thenReturn(List.of(event));
        when(institutionReportDocumentRepository.findByOffenderId(any())).thenReturn(List.of(institutionalReportDocument));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getConvictions()).hasSize(1);
    }

    @Test
    public void twoConvictionsAddedWhenSingleInstitutionalReportAndSingleEventHaveDifferentId() {
        final InstitutionalReportDocument institutionalReportDocument = anInstitutionalReportDocument();
        institutionalReportDocument.getInstitutionalReport().getCustody().getDisposal().getEvent().setEventId(1L);
        final Event event = anEvent();
        event.setEventId(99L);
        final EventDocument cpsPack = anEventDocument(event.getEventId(), DocumentType.CPS_PACK);

        when(eventRepository.findByOffenderId(any())).thenReturn(List.of(event));
        when(eventDocumentRepository.findByOffenderId(any(),eq(DocumentType.CPS_PACK))).thenReturn(List.of(cpsPack));
        when(institutionReportDocumentRepository.findByOffenderId(any())).thenReturn(List.of(institutionalReportDocument));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getConvictions()).hasSize(2);
    }

    @Test
    public void eventDocumentsDistributedToEachConviction() {
        when(eventDocumentRepository.findByOffenderId(any(),eq(DocumentType.DOCUMENT))).thenReturn(List.of(
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
        when(courtReportDocumentRepository.findAll(courtReportDocumentSpecification.capture())).thenReturn(List
            .of(
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
        when(institutionReportDocumentRepository.findByOffenderId(any())).thenReturn(List.of(
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
        when(addressAssessmentRepository.findByOffenderId(any())).thenReturn(List.of(
            anAddressAssessmentDocument(),
            anAddressAssessmentDocument()
        ));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getDocuments()).hasSize(2);
    }

    @Test
    public void approvedPremisesReferralDocumentsDistributedToEachConviction() {
        when(approvedPremisesReferralDocumentRepository.findByOffenderId(any())).thenReturn(List.of(
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
        when(assessmentDocumentRepository.findByOffenderId(any())).thenReturn(List.of(
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
        when(caseAllocationDocumentRepository.findByOffenderId(any())).thenReturn(List.of(
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
        when(personalContactDocumentRepository.findByOffenderId(any())).thenReturn(List.of(
            aPersonalContactDocument(),
            aPersonalContactDocument()
        ));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getDocuments()).hasSize(2);
    }

    @Test
    public void referralDocumentsDistributedToEachConviction() {
        when(referralDocumentRepository.findByOffenderId(any())).thenReturn(List.of(
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
        when(nsiDocumentRepository.findByOffenderId(any())).thenReturn(List.of(
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
        when(personalCircumstanceDocumentRepository.findByOffenderId(any())).thenReturn(List.of(
            aPersonalCircumstanceDocument(),
            aPersonalCircumstanceDocument()
        ));

        final OffenderDocuments documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

        assertThat(documents.getDocuments()).hasSize(2);
    }

    @Test
    public void upwAppointmentDocumentsDistributedToEachConviction() {
        when(upwAppointmentDocumentRepository.findByOffenderId(any())).thenReturn(List.of(
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
        when(contactDocumentRepository.findByOffenderId(any())).thenReturn(List.of(
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

    private List<OffenderDocumentDetail> allDocuments(OffenderDocuments offenderDocuments) {
        final var convictionDocuments =
            offenderDocuments
                .getConvictions()
                .stream()
                .flatMap(cd -> cd.getDocuments().stream())
                .collect(toList());

        return Stream
            .of(offenderDocuments.getDocuments(), convictionDocuments)
            .flatMap(Collection::stream)
            .collect(toList());
    }

    @DisplayName("Filters")
    @Nested
    @MockitoSettings(strictness = Strictness.LENIENT)
    class Filters {
        @BeforeEach
        void setUp() {
            when(eventDocumentRepository.findByOffenderId(any(), eq(DocumentType.DOCUMENT))).thenReturn(List.of(anEventDocument(1L)));
            when(eventDocumentRepository.findByOffenderId(any(), eq(DocumentType.CPS_PACK))).thenReturn(List.of(anEventDocument(1L, DocumentType.CPS_PACK)));
            when(courtReportDocumentRepository.findAll(courtReportDocumentSpecification.capture())).thenReturn(List.of(aCourtReportDocument(1L)));
            when(institutionReportDocumentRepository.findByOffenderId(any())).thenReturn(List.of(anInstitutionalReportDocument(1L)));
            when(approvedPremisesReferralDocumentRepository.findByOffenderId(any())).thenReturn(List.of(anApprovedPremisesReferralDocument(1L)));
            when(assessmentDocumentRepository.findByOffenderId(any())).thenReturn(List.of(anAssessmentDocument(1L)));
            when(caseAllocationDocumentRepository.findByOffenderId(any())).thenReturn(List.of(aCaseAllocationDocument(1L)));
            when(referralDocumentRepository.findByOffenderId(any())).thenReturn(List.of(aReferralDocument(1L)));
            when(nsiDocumentRepository.findByOffenderId(any())).thenReturn(List.of(aNsiDocument(1L), aNsiDocument()));
            when(upwAppointmentDocumentRepository.findByOffenderId(any())).thenReturn(List.of(aUPWAppointmentDocument(1L)));
            when(contactDocumentRepository.findByOffenderId(any())).thenReturn(List.of(aContactDocument(1L), aContactDocument()));
            when(offenderDocumentRepository.findByOffenderId(any(), eq(DocumentType.DOCUMENT))).thenReturn(List.of(anOffenderDocument()));
            when(offenderDocumentRepository.findByOffenderId(any(), eq(DocumentType.PREVIOUS_CONVICTION))).thenReturn(List.of(anOffenderDocument(DocumentType.PREVIOUS_CONVICTION)));
            when(addressAssessmentRepository.findByOffenderId(any())).thenReturn(List.of(anAddressAssessmentDocument()));
            when(personalContactDocumentRepository.findByOffenderId(any())).thenReturn(List.of(aPersonalContactDocument()));
            when(personalCircumstanceDocumentRepository.findByOffenderId(any())).thenReturn(List.of(aPersonalCircumstanceDocument()));
            when(offenderRepository.findByOffenderId(any())).thenReturn(Optional.of(anOffender()
                .toBuilder()
                .build()));
            when(eventRepository.findByOffenderId(any())).thenReturn(List.of(anEvent()
                .toBuilder()
                .eventId(1L)
                .build()));
        }

        @DisplayName("No filter supplied")
        @Nested
        class NoFilterSupplied {
            @Test
            @DisplayName("When no filter all repositories are queried")
            void whenNoFilterAllRepositoriesAreQueried() {
                documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());

                verify(eventDocumentRepository).findByOffenderId(1L, DocumentType.DOCUMENT);
                verify(eventDocumentRepository).findByOffenderId(1L, DocumentType.CPS_PACK.DOCUMENT);
                verify(courtReportDocumentRepository).findAll(courtReportDocumentSpecification.capture());
                verify(institutionReportDocumentRepository).findByOffenderId(1L);
                verify(approvedPremisesReferralDocumentRepository).findByOffenderId(1L);
                verify(assessmentDocumentRepository).findByOffenderId(1L);
                verify(caseAllocationDocumentRepository).findByOffenderId(1L);
                verify(referralDocumentRepository).findByOffenderId(1L);
                verify(nsiDocumentRepository).findByOffenderId(1L);
                verify(upwAppointmentDocumentRepository).findByOffenderId(1L);
                verify(contactDocumentRepository).findByOffenderId(1L);
                verify(offenderDocumentRepository).findByOffenderId(1L, DocumentType.DOCUMENT);
                verify(offenderDocumentRepository).findByOffenderId(1L, DocumentType.PREVIOUS_CONVICTION);
                verify(addressAssessmentRepository).findByOffenderId(1L);
                verify(personalContactDocumentRepository).findByOffenderId(1L);
                verify(personalCircumstanceDocumentRepository).findByOffenderId(1L);
            }

            @Test
            @DisplayName("Will return all documents types")
            void willReturnAllDocumentsTypes() {
                final var documents = documentService.offenderDocumentsFor(1L, DocumentFilter.noFilter());
                assertThat(documents.getConvictions()).hasSize(1);
                assertThat(documents.getConvictions().get(0).getDocuments()).hasSize(11);
                assertThat(documents.getDocuments()).hasSize(7);
            }
        }

        @DisplayName("Filter with just type")
        @Nested
        @TestInstance(PER_CLASS)
        class FilterWithJustType {
            @Test
            @DisplayName("Will only query the repository related to the type")
            void willOnlyQueryTheRepositoryRelatedToTheCourtReportType() {
                documentService.offenderDocumentsFor(1L, DocumentFilter.of("COURT_REPORT_DOCUMENT", null).get());

                verify(courtReportDocumentRepository).findAll(courtReportDocumentSpecification.capture());
                verifyNoInteractions(eventDocumentRepository,
                    institutionReportDocumentRepository,
                    approvedPremisesReferralDocumentRepository,
                    assessmentDocumentRepository,
                    caseAllocationDocumentRepository,
                    referralDocumentRepository,
                    nsiDocumentRepository,
                    upwAppointmentDocumentRepository,
                    contactDocumentRepository,
                    offenderDocumentRepository,
                    addressAssessmentRepository,
                    personalContactDocumentRepository,
                    personalCircumstanceDocumentRepository
                );
            }

            @Test
            @DisplayName("Will only query the repository related to other types")
            void willOnlyQueryTheRepositoryRelatedToOtherDocumentTypes() {
                documentService.offenderDocumentsFor(1L, DocumentFilter.of("ASSESSMENT_DOCUMENT", null).get());

                verify(assessmentDocumentRepository).findByOffenderId(any());
                verifyNoInteractions(eventDocumentRepository,
                    institutionReportDocumentRepository,
                    approvedPremisesReferralDocumentRepository,
                    courtReportDocumentRepository,
                    caseAllocationDocumentRepository,
                    referralDocumentRepository,
                    nsiDocumentRepository,
                    upwAppointmentDocumentRepository,
                    contactDocumentRepository,
                    offenderDocumentRepository,
                    addressAssessmentRepository,
                    personalContactDocumentRepository,
                    personalCircumstanceDocumentRepository
                );
            }

            @Test
            @DisplayName("Will only return documents for the type")
            void willOnlyReturnDocumentsForTheType() {
                final var documents = documentService.offenderDocumentsFor(1L, DocumentFilter
                    .of("COURT_REPORT_DOCUMENT", null)
                    .get());
                assertThat(documents.getConvictions()).hasSize(1);
                assertThat(documents.getConvictions().get(0).getDocuments()).hasSize(1);
                assertThat(documents.getDocuments()).hasSize(0);
            }

            @DisplayName("Supplying a type will only bring back documents of that type")
            @ParameterizedTest(name = "Type {0}")
            @MethodSource("allTypes")
            void supplyingATypeWillOnlyBringBackDocumentsOfThatType(String type) {
                final var documents = documentService.offenderDocumentsFor(1L, DocumentFilter
                    .of(type, null)
                    .get());
                // some types have 1 others 2
                assertThat(allDocuments(documents)).hasSizeBetween(1, 2);
                // all documents have the right type
                assertThat(allDocuments(documents))
                    .extracting(document -> document.getType().getCode())
                    .containsAnyOf(type);
            }

            private Stream<String> allTypes() {
                return Arrays.stream(Type.values()).map(Enum::name);
            }
        }
    }

}
