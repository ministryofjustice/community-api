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
    public void createdAtCopiedWhenPresent() {
        final OffenderDocument offenderDocument = anOffenderDocument();
        offenderDocument.setCreatedDate(LocalDateTime.of(1965, 7, 19, 0, 0, 0));
        offenderDocument.setLastSaved(LocalDateTime.of(1985, 7, 19, 0, 0, 0));

        assertThat(documentTransformer.offenderDocumentsDetailsOfOffenderDocuments(ImmutableList.of(offenderDocument))).hasSize(1);
        assertThat(documentTransformer.offenderDocumentsDetailsOfOffenderDocuments(ImmutableList.of(offenderDocument)).get(0).getCreatedAt()).isEqualTo(LocalDateTime.of(1965, 7, 19, 0, 0, 0));
    }

    @Test
    public void lastSavedCopiedWhenCreateNotPresent() {
        final OffenderDocument offenderDocument = anOffenderDocument();
        offenderDocument.setCreatedDate(null);
        offenderDocument.setLastSaved(LocalDateTime.of(1985, 7, 19, 0, 0, 0));

        assertThat(documentTransformer.offenderDocumentsDetailsOfOffenderDocuments(ImmutableList.of(offenderDocument))).hasSize(1);
        assertThat(documentTransformer.offenderDocumentsDetailsOfOffenderDocuments(ImmutableList.of(offenderDocument)).get(0).getCreatedAt()).isEqualTo(LocalDateTime.of(1985, 7, 19, 0, 0, 0));
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

    @Test
    public void addressAssessmentDescriptionsSet() {
        final AddressAssessmentDocument document = anAddressAssessmentDocument();

        document.getAddressAssessment().setAssessmentDate(LocalDateTime.of(1965, 7, 19, 0, 0));

        assertThat(documentTransformer.offenderDocumentsDetailsOfAddressAssessmentDocuments(ImmutableList.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = documentTransformer.offenderDocumentsDetailsOfAddressAssessmentDocuments(ImmutableList.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("ADDRESS_ASSESSMENT_DOCUMENT");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Address assessment related document");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Address assessment on 19/07/1965");
    }

    @Test
    public void approvedPremisesReferralDescriptionsSet() {
        final ApprovedPremisesReferralDocument document = anApprovedPremisesReferralDocument(1L);

        document.getApprovedPremisesReferral().setReferralDate(LocalDateTime.of(1965, 7, 19, 0, 0));

        assertThat(documentTransformer.offenderDocumentsDetailsOfApprovedPremisesReferralDocuments(ImmutableList.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = documentTransformer.offenderDocumentsDetailsOfApprovedPremisesReferralDocuments(ImmutableList.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("APPROVED_PREMISES_REFERRAL_DOCUMENT");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Approved premises referral related document");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Approved premises referral on 19/07/1965");
    }

    @Test
    public void assessmentDescriptionsSet() {
        final AssessmentDocument document = anAssessmentDocument(1L);

        document.getAssessment().setAssessmentDate(LocalDateTime.of(1965, 7, 19, 0, 0));
        document.getAssessment().getAssessmentType().setDescription("Drugs testing");

        assertThat(documentTransformer.offenderDocumentsDetailsOfAssessmentDocuments(ImmutableList.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = documentTransformer.offenderDocumentsDetailsOfAssessmentDocuments(ImmutableList.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("ASSESSMENT_DOCUMENT");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Assessment document");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Assessment for Drugs testing on 19/07/1965");
    }

    @Test
    public void caseAllocationDescriptionsSet() {
        final CaseAllocationDocument document = aCaseAllocationDocument(1L);


        assertThat(documentTransformer.offenderDocumentsDetailsOfCaseAllocationDocuments(ImmutableList.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = documentTransformer.offenderDocumentsDetailsOfCaseAllocationDocuments(ImmutableList.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("CASE_ALLOCATION_DOCUMENT");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Case allocation document");
    }

    @Test
    public void personalContactDescriptionsSet() {
        final PersonalContactDocument document = aPersonalContactDocument();


        assertThat(documentTransformer.offenderDocumentsDetailsOfPersonalContactDocuments(ImmutableList.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = documentTransformer.offenderDocumentsDetailsOfPersonalContactDocuments(ImmutableList.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("PERSONAL_CONTACT_DOCUMENT");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Personal contact of type GP with Father");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Personal contact related document");
    }

    @Test
    public void referralDescriptionsSet() {
        final ReferralDocument document = aReferralDocument(1L);

        document.getReferral().getReferralType().setDescription("Mental Health");
        document.getReferral().setReferralDate(LocalDateTime.of(1965, 7, 19, 0, 0));


        assertThat(documentTransformer.offenderDocumentsDetailsOfReferralDocuments(ImmutableList.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = documentTransformer.offenderDocumentsDetailsOfReferralDocuments(ImmutableList.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("REFERRAL_DOCUMENT");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Referral for Mental Health on 19/07/1965");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Referral related document");
    }

    @Test
    public void nsiDescriptionsSet() {
        final NsiDocument document = aNsiDocument(1L);

        document.getNsi().getNsiSubType().setCodeDescription("Healthy Sex Programme (HCP)");
        document.getNsi().setReferralDate(LocalDate.of(1965, 7, 19));


        assertThat(documentTransformer.offenderDocumentsDetailsOfNsiDocuments(ImmutableList.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = documentTransformer.offenderDocumentsDetailsOfNsiDocuments(ImmutableList.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("NSI_DOCUMENT");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Non Statutory Intervention for Healthy Sex Programme (HCP) on 19/07/1965");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Non Statutory Intervention related document");
    }

    @Test
    public void personalCircumstanceDescriptionsSet() {
        final PersonalCircumstanceDocument document = aPersonalCircumstanceDocument();

        document.getPersonalCircumstance().getCircumstanceType().setCodeDescription("AP - Medication in Posession - Assessment");
        document.getPersonalCircumstance().setStartDate(LocalDate.of(1965, 7, 19));


        assertThat(documentTransformer.offenderDocumentsDetailsOfPersonalCircumstanceDocuments(ImmutableList.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = documentTransformer.offenderDocumentsDetailsOfPersonalCircumstanceDocuments(ImmutableList.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("PERSONAL_CIRCUMSTANCE_DOCUMENT");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Personal circumstance of AP - Medication in Posession - Assessment started on 19/07/1965");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Personal circumstance related document");
    }

    @Test
    public void upwAppointmentDescriptionsSet() {
        final UPWAppointmentDocument document = aUPWAppointmentDocument(1L);

        document.getUpwAppointment().getUpwProject().setName("Cutting grass");
        document.getUpwAppointment().setAppointmentDate(LocalDateTime.of(1965, 7, 19, 0, 0));


        assertThat(documentTransformer.offenderDocumentsDetailsOfUPWAppointmentDocuments(ImmutableList.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = documentTransformer.offenderDocumentsDetailsOfUPWAppointmentDocuments(ImmutableList.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("UPW_APPOINTMENT_DOCUMENT");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Unpaid work appointment on 19/07/1965 for Cutting grass");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Unpaid work appointment document");
    }

    @Test
    public void contactDescriptionsSet() {
        final ContactDocument document = aContactDocument();

        document.getContact().getContactType().setDescription("Meet up");
        document.getContact().setContactDate(LocalDate.of(1965, 7, 19));


        assertThat(documentTransformer.offenderDocumentsDetailsOfContactDocuments(ImmutableList.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = documentTransformer.offenderDocumentsDetailsOfContactDocuments(ImmutableList.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("CONTACT_DOCUMENT");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Contact on 19/07/1965 for Meet up");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Contact related document");
    }


}