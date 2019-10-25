package uk.gov.justice.digital.delius.util;

import lombok.val;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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

    private static InstitutionalReport anInstitutionalReport() {
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
        return anEvent(100L);
    }

    public static Event anEvent(Long eventId) {
        return anEvent(eventId, 777L);
    }

    public static Event anEvent(Long eventId, Long offenderId) {
        return Event
                .builder()
                .eventId(eventId)
                .offenderId(offenderId)
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
                .softDeleted(0L)
                .activeFlag(1L)
                .build();
    }

    public static Event aCustodyEvent() {
        return aCustodyEvent(100L, 99L, new ArrayList<>());
    }

    public static Event aCustodyEvent(Long eventId, List<KeyDate> keyDates) {
        return aCustodyEvent(eventId, 99L, keyDates);
    }

    public static Event aCustodyEvent(Long eventId, Long offenderId, List<KeyDate> keyDates) {
        val disposal = aDisposal(eventId);
        return anEvent(eventId, offenderId)
                .toBuilder()
                .disposal(aCustodialDisposal(keyDates, disposal))
                .build();
    }

    private static Disposal aCustodialDisposal(List<KeyDate> keyDates, Disposal disposal) {
        return disposal
                .toBuilder()
                .disposalType(DisposalType
                        .builder()
                        .sentenceType("NC")
                        .build())
                .custody(aCustody(disposal, keyDates))
                .build();
    }

    private static Custody aCustody(Disposal disposal, List<KeyDate> keyDates) {
        return Custody
                .builder()
                .disposal(disposal)
                .custodyId(9999L)
                .keyDates(keyDates)
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

    private static CourtReport aCourtReport() {
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

    private static void populateBasics(Document document) {
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

    public static AddressAssessmentDocument anAddressAssessmentDocument() {
        final AddressAssessmentDocument document = new AddressAssessmentDocument();
        populateBasics(document);
        document.setAddressAssessment(anAddressAssessment());
        return document;
    }

    private static AddressAssessment anAddressAssessment() {
        return AddressAssessment
                .builder()
                .assessmentDate(LocalDateTime.now())
                .build();
    }

    public static ApprovedPremisesReferralDocument anApprovedPremisesReferralDocument(Long eventId) {
        final ApprovedPremisesReferralDocument document = new ApprovedPremisesReferralDocument();
        populateBasics(document);
        document.setApprovedPremisesReferral(anApprovedPremisesReferral(eventId));
        return document;
    }

    private static ApprovedPremisesReferral anApprovedPremisesReferral(Long eventId) {
        return ApprovedPremisesReferral
                .builder()
                .referralDate(LocalDateTime.now())
                .event(anEvent(eventId))
                .build();
    }

    public static AssessmentDocument anAssessmentDocument(Long eventId) {
        final AssessmentDocument document = new AssessmentDocument();
        populateBasics(document);
        document.setAssessment(anAssessment(eventId));
        return document;
    }

    private static Assessment anAssessment(Long eventId) {
        return Assessment
                .builder()
                .assessmentDate(LocalDateTime.now())
                .assessmentType(RAssessmentType
                        .builder()
                        .description("Drug testing")
                        .build())
                .referral(aReferral(eventId))
                .build();
    }

    public static CaseAllocationDocument aCaseAllocationDocument(Long eventId) {
        final CaseAllocationDocument document = new CaseAllocationDocument();
        populateBasics(document);
        document.setCaseAllocation(aCaseAllocation(eventId));
        return document;
    }

    private static CaseAllocation aCaseAllocation(Long eventId) {
        return CaseAllocation
                .builder()
                .event(anEvent(eventId))
                .build();
    }

    public static PersonalContactDocument aPersonalContactDocument() {
        final PersonalContactDocument document = new PersonalContactDocument();
        populateBasics(document);
        document.setPersonalContact(aPersonalContact());
        return document;
    }

    private static PersonalContact aPersonalContact() {
        return PersonalContact
                .builder()
                .relationship("Father")
                .relationshipType(
                        StandardReference
                                .builder()
                                .codeDescription("GP")
                                .build()
                )
                .build();
    }

    public static ReferralDocument aReferralDocument(Long eventId) {
        final ReferralDocument document = new ReferralDocument();
        populateBasics(document);
        document.setReferral(aReferral(eventId));
        return document;
    }

    private static Referral aReferral(Long eventId) {
        return Referral
                .builder()
                .event(anEvent(eventId))
                .referralDate(LocalDateTime.now())
                .referralType(RReferralType
                        .builder()
                        .description("Mental Health")
                        .build())
                .build();
    }

    public static NsiDocument aNsiDocument(Long eventId) {
        final NsiDocument document = new NsiDocument();
        populateBasics(document);
        document.setNsi(aNsi(eventId));
        return document;
    }

    public static NsiDocument aNsiDocument() {
        final NsiDocument document = new NsiDocument();
        populateBasics(document);
        document.setNsi(aNsi());
        return document;
    }


    private static Nsi aNsi(Long eventId) {
        return aNsi()
                .toBuilder()
                .event(anEvent(eventId))
                .build();
    }

    private static Nsi aNsi() {
        return Nsi
                .builder()
                .nsiType(NsiType
                        .builder()
                        .description("Custody - Accredited Programme")
                        .build())
                .nsiSubType(StandardReference
                        .builder()
                        .codeDescription("Healthy Sex Programme (HCP)")
                        .build())
                .referralDate(LocalDate.now())
                .build();
    }

    public static PersonalCircumstanceDocument aPersonalCircumstanceDocument() {
        final PersonalCircumstanceDocument document = new PersonalCircumstanceDocument();
        populateBasics(document);
        document.setPersonalCircumstance(aPersonalCircumstance());
        return document;
    }

    private static PersonalCircumstance aPersonalCircumstance() {
        return PersonalCircumstance
                .builder()
                .startDate(LocalDate.now())
                .circumstanceType(CircumstanceType
                        .builder()
                        .codeDescription("AP - Medication in Posession - Assessment")
                        .build())
                .circumstanceSubType(CircumstanceSubType
                        .builder()
                        .codeDescription("MiP approved")
                        .build())
                .build();
    }

    public static UPWAppointmentDocument aUPWAppointmentDocument(Long eventId) {
        final UPWAppointmentDocument document = new UPWAppointmentDocument();
        populateBasics(document);
        document.setUpwAppointment(upwAppointment(eventId));
        return document;
    }


    private static UpwAppointment upwAppointment(Long eventId) {
        return UpwAppointment
                .builder()
                .appointmentDate(LocalDateTime.now())
                .upwDetails(UpwDetails
                        .builder()
                        .disposal(aDisposal(eventId))
                        .build())
                .upwProject(UpwProject
                        .builder()
                        .name("Grass cutting")
                        .build())
                .build();
    }

    private static Disposal aDisposal(Long eventId) {
        return Disposal
                .builder()
                .disposalType(
                        DisposalType
                                .builder()
                                .sentenceType("SC")
                                .build())
                .event(anEvent(eventId))
                .build();
    }

    public static Disposal aCommunityDisposal(Long eventId) {
        return aDisposal(eventId)
                .toBuilder()
                .disposalType(
                        DisposalType
                                .builder()
                                .sentenceType("SP")
                                .build())
                .build();
    }

    public static ContactDocument aContactDocument(Long eventId) {
        final ContactDocument document = new ContactDocument();
        populateBasics(document);
        document.setContact(aContact(eventId));
        return document;
    }

    public static ContactDocument aContactDocument() {
        final ContactDocument document = new ContactDocument();
        populateBasics(document);
        document.setContact(aContact());
        return document;
    }

    private static Contact aContact(Long eventId) {
        return Contact
                .builder()
                .event(anEvent(eventId))
                .contactDate(LocalDate.now())
                .contactStartTime(LocalTime.now())
                .contactType(ContactType
                        .builder()
                        .description("Case Conference - MAPPA")
                        .build())
                .build();
    }

    private static Contact aContact() {
        return Contact
                .builder()
                .contactDate(LocalDate.now())
                .contactStartTime(LocalTime.now())
                .contactType(ContactType
                        .builder()
                        .description("Offered Female OM - Accepted")
                        .build())
                .build();

    }

    public static KeyDate aKeyDate(String typeCode, String description, LocalDate date) {
        return KeyDate
                .builder()
                .keyDate(date)
                .keyDateType(StandardReference
                        .builder()
                        .codeDescription(description)
                        .codeValue(typeCode)
                        .build())
                .build();
    }

    public static KeyDate aKeyDate(Long keyDateId, String typeCode) {
        return KeyDate
                .builder()
                .keyDateId(keyDateId)
                .keyDate(LocalDate.now())
                .keyDateType(StandardReference
                        .builder()
                        .codeDescription("description")
                        .codeValue(typeCode)
                        .build())
                .build();
    }
}
