package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.OffenderDocumentDetail;
import uk.gov.justice.digital.delius.jpa.standard.entity.AddressAssessmentDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.ApprovedPremisesReferralDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.AssessmentDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.CaseAllocationDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtReportDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReportDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.NsiDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.PersonalCircumstanceDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.PersonalContactDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.ReferralDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.UPWAppointmentDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import static uk.gov.justice.digital.delius.util.EntityHelper.anInstitutionalReportDocument;
import static uk.gov.justice.digital.delius.util.EntityHelper.anOffenderDocument;

public class DocumentTransformerTest {
    private DocumentTransformer documentTransformer;

    @BeforeEach
    public void before() {
        documentTransformer = new DocumentTransformer();
    }

    @Test
    public void createdAtCopiedWhenPresent() {
        final OffenderDocument offenderDocument = anOffenderDocument();
        offenderDocument.setCreatedDate(LocalDateTime.of(1965, 7, 19, 0, 0, 0));
        offenderDocument.setLastSaved(LocalDateTime.of(1985, 7, 19, 0, 0, 0));

        assertThat(DocumentTransformer.offenderDocumentsDetailsOfOffenderDocuments(List.of(offenderDocument))).hasSize(1);
        assertThat(DocumentTransformer.offenderDocumentsDetailsOfOffenderDocuments(List.of(offenderDocument)).get(0).getCreatedAt()).isEqualTo(LocalDateTime.of(1965, 7, 19, 0, 0, 0));
    }

    @Test
    public void lastSavedCopiedWhenCreateNotPresent() {
        final OffenderDocument offenderDocument = anOffenderDocument();
        offenderDocument.setCreatedDate(null);
        offenderDocument.setLastSaved(LocalDateTime.of(1985, 7, 19, 0, 0, 0));

        assertThat(DocumentTransformer.offenderDocumentsDetailsOfOffenderDocuments(List.of(offenderDocument))).hasSize(1);
        assertThat(DocumentTransformer.offenderDocumentsDetailsOfOffenderDocuments(List.of(offenderDocument)).get(0).getCreatedAt()).isEqualTo(LocalDateTime.of(1985, 7, 19, 0, 0, 0));
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

        assertThat(DocumentTransformer.offenderDocumentsDetailsOfOffenderDocuments(List.of(offenderDocument))).hasSize(1);
        assertThat(DocumentTransformer.offenderDocumentsDetailsOfOffenderDocuments(List.of(offenderDocument)).get(0).getAuthor()).isEqualTo("Bobby Bread");
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

        assertThat(DocumentTransformer.offenderDocumentsDetailsOfOffenderDocuments(List.of(offenderDocument))).hasSize(1);
        assertThat(DocumentTransformer.offenderDocumentsDetailsOfOffenderDocuments(List.of(offenderDocument))
                .get(0).getAuthor()).isEqualTo("Lardy Lump");
    }

    @Test
    public void courtReportDescriptionsSet() {
        final CourtReportDocument document = aCourtReportDocument();

        document.getCourtReport().setDateRequested(LocalDateTime.of(1965, 7, 19, 0, 0));
        document.getCourtReport().getCourtAppearance().getCourt().setCourtName("Sheffield Crown Court");

        assertThat(DocumentTransformer.offenderDocumentsDetailsOfCourtReportDocuments(List.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = DocumentTransformer
                .offenderDocumentsDetailsOfCourtReportDocuments(List.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("COURT_REPORT_DOCUMENT");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Court report");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Pre-Sentence Report - Standard requested by Sheffield Crown Court on 19/07/1965");
        assertThat(offenderDocumentDetail.getSubType().getCode()).isEqualTo("CJS");
        assertThat(offenderDocumentDetail.getSubType().getDescription()).isEqualTo("Pre-Sentence Report - Standard");
    }

    @Test
    public void institutionReportDescriptionsSet() {
        final InstitutionalReportDocument document = anInstitutionalReportDocument();
        document.getInstitutionalReport().setDateRequested(LocalDate.of(1965, 7, 19));
        document.getInstitutionalReport().setDateRequired(LocalDate.of(1965, 7, 20));
        document.getInstitutionalReport().setDateCompleted(LocalDate.of(1965, 7, 21));
        document.getInstitutionalReport().getInstitution().setInstitutionName("Sheffield jail");

        assertThat(DocumentTransformer
                .offenderDocumentsDetailsOfInstitutionReportDocuments(List.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = DocumentTransformer
                .offenderDocumentsDetailsOfInstitutionReportDocuments(List.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("INSTITUTION_REPORT_DOCUMENT");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Institution report");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Parole Assessment Report at Sheffield jail requested on 19/07/1965");
        assertThat(offenderDocumentDetail.getSubType().getCode()).isEqualTo("PAR");
        assertThat(offenderDocumentDetail.getSubType().getDescription()).isEqualTo("Parole Assessment Report");
        assertNotNull(offenderDocumentDetail.getReportDocumentDates().getRequestedDate());
        assertNotNull(offenderDocumentDetail.getReportDocumentDates().getRequiredDate());
        assertNotNull(offenderDocumentDetail.getReportDocumentDates().getCompletedDate());
    }

    @Test
    public void addressAssessmentDescriptionsSet() {
        final AddressAssessmentDocument document = anAddressAssessmentDocument();

        document.getAddressAssessment().setAssessmentDate(LocalDateTime.of(1965, 7, 19, 0, 0));

        assertThat(DocumentTransformer
                .offenderDocumentsDetailsOfAddressAssessmentDocuments(List.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = DocumentTransformer
                .offenderDocumentsDetailsOfAddressAssessmentDocuments(List.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("ADDRESS_ASSESSMENT_DOCUMENT");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Address assessment related document");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Address assessment on 19/07/1965");
    }

    @Test
    public void approvedPremisesReferralDescriptionsSet() {
        final ApprovedPremisesReferralDocument document = anApprovedPremisesReferralDocument(1L);

        document.getApprovedPremisesReferral().setReferralDate(LocalDateTime.of(1965, 7, 19, 0, 0));

        assertThat(DocumentTransformer
                .offenderDocumentsDetailsOfApprovedPremisesReferralDocuments(List.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = DocumentTransformer
                .offenderDocumentsDetailsOfApprovedPremisesReferralDocuments(List.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("APPROVED_PREMISES_REFERRAL_DOCUMENT");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Approved premises referral related document");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Approved premises referral on 19/07/1965");
    }

    @Test
    public void assessmentDescriptionsSet() {
        final AssessmentDocument document = anAssessmentDocument(1L);

        document.getAssessment().setAssessmentDate(LocalDateTime.of(1965, 7, 19, 0, 0));
        document.getAssessment().getAssessmentType().setDescription("Drugs testing");

        assertThat(DocumentTransformer.offenderDocumentsDetailsOfAssessmentDocuments(List.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = DocumentTransformer
                .offenderDocumentsDetailsOfAssessmentDocuments(List.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("ASSESSMENT_DOCUMENT");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Assessment document");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Assessment for Drugs testing on 19/07/1965");
    }

    @Test
    public void caseAllocationDescriptionsSet() {
        final CaseAllocationDocument document = aCaseAllocationDocument(1L);


        assertThat(DocumentTransformer
                .offenderDocumentsDetailsOfCaseAllocationDocuments(List.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = DocumentTransformer
                .offenderDocumentsDetailsOfCaseAllocationDocuments(List.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("CASE_ALLOCATION_DOCUMENT");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Case allocation document");
    }

    @Test
    public void personalContactDescriptionsSet() {
        final PersonalContactDocument document = aPersonalContactDocument();


        assertThat(DocumentTransformer
                .offenderDocumentsDetailsOfPersonalContactDocuments(List.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = DocumentTransformer
                .offenderDocumentsDetailsOfPersonalContactDocuments(List.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("PERSONAL_CONTACT_DOCUMENT");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Personal contact of type Drug Worker with Father");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Personal contact related document");
    }

    @Test
    public void referralDescriptionsSet() {
        final ReferralDocument document = aReferralDocument(1L);

        document.getReferral().getReferralType().setDescription("Mental Health");
        document.getReferral().setReferralDate(LocalDateTime.of(1965, 7, 19, 0, 0));


        assertThat(DocumentTransformer.offenderDocumentsDetailsOfReferralDocuments(List.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = DocumentTransformer
                .offenderDocumentsDetailsOfReferralDocuments(List.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("REFERRAL_DOCUMENT");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Referral for Mental Health on 19/07/1965");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Referral related document");
    }

    @Test
    public void nsiDescriptionsSet() {
        final NsiDocument document = aNsiDocument(1L);

        document.getNsi().getNsiType().setDescription("Custody - Accredited Programme");
        document.getNsi().setReferralDate(LocalDate.of(1965, 7, 19));


        assertThat(DocumentTransformer.offenderDocumentsDetailsOfNsiDocuments(List.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = DocumentTransformer
                .offenderDocumentsDetailsOfNsiDocuments(List.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("NSI_DOCUMENT");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Non Statutory Intervention for Custody - Accredited Programme on 19/07/1965");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Non Statutory Intervention related document");
        assertThat(offenderDocumentDetail.getParentPrimaryKeyId()).isEqualTo(100L);
    }

    @Test
    public void personalCircumstanceDescriptionsSet() {
        final PersonalCircumstanceDocument document = aPersonalCircumstanceDocument();

        document.getPersonalCircumstance().getCircumstanceType().setCodeDescription("AP - Medication in Posession - Assessment");
        document.getPersonalCircumstance().setStartDate(LocalDate.of(1965, 7, 19));


        assertThat(DocumentTransformer.offenderDocumentsDetailsOfPersonalCircumstanceDocuments(List.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = DocumentTransformer.offenderDocumentsDetailsOfPersonalCircumstanceDocuments(List.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("PERSONAL_CIRCUMSTANCE_DOCUMENT");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Personal circumstance of AP - Medication in Posession - Assessment started on 19/07/1965");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Personal circumstance related document");
    }

    @Test
    public void upwAppointmentDescriptionsSet() {
        final UPWAppointmentDocument document = aUPWAppointmentDocument(1L);

        document.getUpwAppointment().getUpwProject().setName("Cutting grass");
        document.getUpwAppointment().setAppointmentDate(LocalDateTime.of(1965, 7, 19, 0, 0));


        assertThat(DocumentTransformer.offenderDocumentsDetailsOfUPWAppointmentDocuments(List.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = DocumentTransformer.offenderDocumentsDetailsOfUPWAppointmentDocuments(List.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("UPW_APPOINTMENT_DOCUMENT");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Unpaid work appointment on 19/07/1965 for Cutting grass");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Unpaid work appointment document");
    }

    @Test
    public void contactDescriptionsSet() {
        final ContactDocument document = aContactDocument();

        document.getContact().getContactType().setDescription("Meet up");
        document.getContact().setContactDate(LocalDate.of(1965, 7, 19));


        assertThat(DocumentTransformer.offenderDocumentsDetailsOfContactDocuments(List.of(document))).hasSize(1);

        final OffenderDocumentDetail offenderDocumentDetail = DocumentTransformer.offenderDocumentsDetailsOfContactDocuments(List.of(document)).get(0);

        assertThat(offenderDocumentDetail.getType().getCode()).isEqualTo("CONTACT_DOCUMENT");
        assertThat(offenderDocumentDetail.getExtendedDescription()).isEqualTo("Contact on 19/07/1965 for Meet up");
        assertThat(offenderDocumentDetail.getType().getDescription()).isEqualTo("Contact related document");
    }


}
