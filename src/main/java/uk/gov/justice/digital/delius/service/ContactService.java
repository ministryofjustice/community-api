package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.Contact;
import uk.gov.justice.digital.delius.data.api.ContactSummary;
import uk.gov.justice.digital.delius.jpa.filters.ContactFilter;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.OrderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.PrisonOffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;
import uk.gov.justice.digital.delius.transformers.ContactTransformer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static uk.gov.justice.digital.delius.jpa.standard.entity.Contact.*;

@Service
@AllArgsConstructor
public class ContactService {

    private static final String PRISONER_OFFENDER_MANAGER_ALLOCATION_CONTACT_TYPE = "EPOMAT";
    private static final String PRISONER_OFFENDER_MANAGER_INTERNAL_ALLOCATION_CONTACT_TYPE = "EPOMIN";
    private static final String PRISONER_OFFENDER_MANAGER_EXTERNAL_ALLOCATION_CONTACT_TYPE = "EPOMEX";
    private static final String RESPONSIBLE_OFFICER_CHANGE_CONTACT_TYPE = "ROC";
    private static final String PRISON_LOCATION_CHANGE_CONTACT_TYPE = "ETCP";
    private static final String CUSTODY_AUTO_UPDATE_CONTACT_TYPE = "EDSS";
    private static final String TIER_UPDATE_CONTACT_TYPE = "ETCH20";
    public static final String DELIUS_DATE_FORMAT = "E MMM dd yyyy"; // e.g. "Tue Nov 24 2020"
    private final ContactRepository contactRepository;
    private final ContactTypeRepository contactTypeRepository;

    public List<Contact> contactsFor(final Long offenderId, final ContactFilter filter) {
        return ContactTransformer.contactsOf(contactRepository.findAll(filter.toBuilder().offenderId(offenderId).build()));
    }

    public Page<ContactSummary> contactSummariesFor(final Long offenderId, final ContactFilter filter, final int page, final int pageSize) {
        final var pagination = PageRequest.of(page, pageSize, Sort.by(DESC, "contactDate", "contactStartTime", "contactEndTime"));
        return contactRepository.findAll(filter.toBuilder().offenderId(offenderId).build(), pagination)
            .map(ContactTransformer::contactSummaryOf);
    }

    @Transactional
    public void addContactForPOMAllocation(final PrisonOffenderManager newPrisonOffenderManager) {
        contactRepository.save(contactForPOMAllocation(newPrisonOffenderManager));
    }

    @Transactional
    public void addContactForPOMAllocation(final PrisonOffenderManager newPrisonOffenderManager, final PrisonOffenderManager existingPrisonOffenderManager) {
        final var contact = contactForPOMAllocation(newPrisonOffenderManager);

        contactRepository.save(contact
                .toBuilder()
                .notes(appendNoteForExistingPOMAllocation(contact.getNotes(), existingPrisonOffenderManager))
                .build());
    }

    @Transactional
    public void addContactForResponsibleOfficerChange(final PrisonOffenderManager newPrisonOffenderManager, final PrisonOffenderManager existingPrisonOffenderManager) {
        final var contactType = contactTypeForResponsibleOfficerChange();
        contactRepository.save(builder()
                .contactDate(newPrisonOffenderManager.getAllocationDate())
                .contactStartTime(LocalTime.now())
                .offenderId(newPrisonOffenderManager.getOffenderId())
                .notes(notesForResponsibleManager(newPrisonOffenderManager, existingPrisonOffenderManager))
                .team(newPrisonOffenderManager.getTeam())
                .staff(newPrisonOffenderManager.getStaff())
                .probationArea(newPrisonOffenderManager.getProbationArea())
                .staffEmployeeId(newPrisonOffenderManager.getStaff().getStaffId())
                .teamProviderId(newPrisonOffenderManager.getTeam().getTeamId())
                .contactType(contactType)
                .alertActive(contactType.getAlertFlag())
                .build());
    }

    @Transactional
    public void addContactForResponsibleOfficerChange(final OffenderManager newResponsibleOfficer, final PrisonOffenderManager existingResponisbleOfficer) {
        addContactForResponsibleOfficerChange(newResponsibleOfficer.getOffenderId(),
                notesForResponsibleManager(newResponsibleOfficer, existingResponisbleOfficer),
                newResponsibleOfficer.getTeam(),
                newResponsibleOfficer.getStaff(),
                newResponsibleOfficer.getProbationArea());
    }

    @Transactional
    public void addContactForResponsibleOfficerChange(final PrisonOffenderManager newResponsibleOfficer, final OffenderManager existingResponsibleOfficer) {
        addContactForResponsibleOfficerChange(newResponsibleOfficer.getOffenderId(),
                notesForResponsibleManager(newResponsibleOfficer, existingResponsibleOfficer),
                newResponsibleOfficer.getTeam(),
                newResponsibleOfficer.getStaff(),
                newResponsibleOfficer.getProbationArea());
    }

    @Transactional
    public void addContactForPrisonLocationChange(final Offender offender, final Event event) {
        addContactForCustodyChange(offender,
                event,
                contactTypeForPrisonLocationChange(),
                notesForPrisonLocationChange(event));
    }

    @Transactional
    public void addContactForBookingNumberUpdate(final Offender offender, final Event event) {
        addContactForCustodyChange(offender,
                event,
                contactTypeForCustodyAutoUpdate(),
                notesForBookingNumbUpdate(event));
    }

    @Transactional
    public void addContactForBulkCustodyKeyDateUpdate(final Offender offender, final Event event, final Map<String, LocalDate> datesAmendedOrUpdated, final Map<String, LocalDate> datesRemoved) {
        addContactForCustodyChange(offender,
                event,
                contactTypeForCustodyAutoUpdate(),
                notesForKeyDatesUpdate(datesAmendedOrUpdated, datesRemoved));
    }

    @Transactional
    public void addContactForTierUpdate(final Long offenderId, final LocalDateTime date, final String tier, final String reason, final Staff staff, final Team team){
        contactRepository.save(builder()
            .contactDate(LocalDate.now())
            .offenderId(offenderId)
            .contactStartTime(LocalTime.now())
            .notes(String.format(
                "Tier Change Date: %s\n" +
                "Tier: %s\n" + "Tier Change Reason: %s",DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(date),tier,reason))
            .staff(staff)
            .staffEmployeeId(staff.getStaffId())
            .teamProviderId(team.getTeamId())
            .probationArea(team.getProbationArea())
            .team(team)
            .contactType(contactTypeRepository.findByCode(TIER_UPDATE_CONTACT_TYPE).orElseThrow(() -> new NotFoundException("Cannot find contact type for tier update")))
            .build());
    }

    private String notesForKeyDatesUpdate(final Map<String, LocalDate> datesAmendedOrUpdated, final Map<String, LocalDate> datesRemoved) {
        final var notes = datesAmendedOrUpdated.entrySet().stream().map(entry -> String
                .format("%s: %s\n", entry.getKey(), entry.getValue()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))))
                .reduce("", (original, it) -> original + it);

        return datesRemoved.entrySet().stream().map(entry -> String
                .format("Removed %s: %s\n", entry.getKey(), entry.getValue()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))))
                .reduce(notes, (original, it) -> original + it);
    }

    private void addContactForCustodyChange(final Offender offender, final Event event, final ContactType contactType, final String notes) {
        final var mayBeOrderManager = event.getOrderManagers()
                .stream()
                .filter(OrderManager::isActive)
                .findFirst();

        contactRepository.save(builder()
                .contactDate(LocalDate.now())
                .contactStartTime(LocalTime.now())
                .offenderId(offender.getOffenderId())
                .notes(notes)
                .team(mayBeOrderManager.map(OrderManager::getTeam).orElse(null))
                .staff(mayBeOrderManager.map(OrderManager::getStaff).orElse(null))
                .probationArea(mayBeOrderManager.map(OrderManager::getProbationArea).orElse(null))
                .staffEmployeeId(mayBeOrderManager.flatMap(orderManager -> Optional.ofNullable(orderManager.getStaff())).map(Staff::getStaffId).orElse(null))
                .teamProviderId(mayBeOrderManager.flatMap(orderManager -> Optional.ofNullable(orderManager.getTeam())).map(Team::getTeamId).orElse(null))
                .contactType(contactType)
                .alertActive(contactType.getAlertFlag())
                .event(event)
                .build());
    }

    private uk.gov.justice.digital.delius.jpa.standard.entity.Contact contactForPOMAllocation(final PrisonOffenderManager newPrisonOffenderManager) {
        final var contactType = contactTypeForPOMAllocationOf(newPrisonOffenderManager.getAllocationReason());
        return builder()
                .contactDate(newPrisonOffenderManager.getAllocationDate())
                .contactStartTime(LocalTime.now())
                .offenderId(newPrisonOffenderManager.getOffenderId())
                .notes(notesForPOMAllocation(newPrisonOffenderManager))
                .team(newPrisonOffenderManager.getTeam())
                .staff(newPrisonOffenderManager.getStaff())
                .probationArea(newPrisonOffenderManager.getProbationArea())
                .staffEmployeeId(newPrisonOffenderManager.getStaff().getStaffId())
                .teamProviderId(newPrisonOffenderManager.getTeam().getTeamId())
                .contactType(contactType)
                .alertActive(contactType.getAlertFlag())
                .build();
    }

    private ContactType contactTypeForPOMAllocationOf(final StandardReference allocationReason) {
        switch (allocationReason.getCodeValue()) {
            case ReferenceDataService.POM_AUTO_TRANSFER_ALLOCATION_REASON_CODE:
                return contactTypeRepository.findByCode(PRISONER_OFFENDER_MANAGER_ALLOCATION_CONTACT_TYPE).orElseThrow();
            case ReferenceDataService.POM_INTERNAL_TRANSFER_ALLOCATION_REASON_CODE:
                return contactTypeRepository.findByCode(PRISONER_OFFENDER_MANAGER_INTERNAL_ALLOCATION_CONTACT_TYPE).orElseThrow();
            case ReferenceDataService.POM_EXTERNAL_TRANSFER_ALLOCATION_REASON_CODE:
                return contactTypeRepository.findByCode(PRISONER_OFFENDER_MANAGER_EXTERNAL_ALLOCATION_CONTACT_TYPE).orElseThrow();
            default:
                throw new RuntimeException(String.format("Don't know what sort of contact type for POM allocation reason %s", allocationReason.getCodeValue()));
        }
    }

    private ContactType contactTypeForResponsibleOfficerChange() {
         return contactTypeRepository.findByCode(RESPONSIBLE_OFFICER_CHANGE_CONTACT_TYPE).orElseThrow();
    }

    private ContactType contactTypeForPrisonLocationChange() {
         return contactTypeRepository.findByCode(PRISON_LOCATION_CHANGE_CONTACT_TYPE).orElseThrow();
    }

    private ContactType contactTypeForCustodyAutoUpdate() {
        return contactTypeRepository.findByCode(CUSTODY_AUTO_UPDATE_CONTACT_TYPE).orElseThrow();
    }

    private String notesForPOMAllocation(final PrisonOffenderManager newPrisonOffenderManager) {
        return "Transfer Reason: " +
                newPrisonOffenderManager.getAllocationReason().getCodeDescription() +
                "\n" +
                "Transfer Date: " +
                newPrisonOffenderManager.getAllocationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                "\n";
    }

    private String notesForResponsibleManager(final PrisonOffenderManager newPrisonOffenderManager, final PrisonOffenderManager existingPrisonOffenderManager) {
        return String.format(
                "New Details:\n" +
                        "%s\n" +
                        "Previous Details:\n" +
                        "%s\n",
                notesForResponsibleManagerOf(newPrisonOffenderManager),
                notesForResponsibleManagerOf(existingPrisonOffenderManager));
    }

    private String notesForResponsibleManager(final OffenderManager newOffenderManager, final PrisonOffenderManager existingPrisonOffenderManager) {
        return String.format(
                "New Details:\n" +
                        "%s\n" +
                        "Previous Details:\n" +
                        "%s\n",
                notesForResponsibleManagerOf(newOffenderManager),
                notesForResponsibleManagerOf(existingPrisonOffenderManager));
    }

    private String notesForResponsibleManager(final PrisonOffenderManager newPrisonOffenderManager, final OffenderManager existingCommunityOffenderManager) {
        return String.format(
                "New Details:\n" +
                        "%s\n" +
                        "Previous Details:\n" +
                        "%s\n",
                notesForResponsibleManagerOf(newPrisonOffenderManager),
                notesForResponsibleManagerOf(existingCommunityOffenderManager));
    }


    private String notesForResponsibleManagerOf(final PrisonOffenderManager prisonOffenderManager) {
        return String.format(
                "Responsible Officer Type: Prison Offender Manager\n" +
                        "Responsible Officer: %s\n" +
                        "Start Date: %s\n" +
                        "%s" +
                        "Allocation Reason: %s\n",
                responsibleOfficerOf(prisonOffenderManager),
                prisonOffenderManager.getLatestResponsibleOfficer().getStartDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                Optional.ofNullable(prisonOffenderManager.getLatestResponsibleOfficer().getEndDateTime()).map(dateTime -> dateTime.format(DateTimeFormatter.ofPattern("'End Date: 'dd/MM/yyyy HH:mm:ss'\n'"))).orElse(""),
                prisonOffenderManager.getAllocationReason().getCodeDescription())
        ;
    }

    private String notesForResponsibleManagerOf(final OffenderManager offenderManager) {
        return String.format(
                "Responsible Officer Type: Offender Manager\n" +
                        "Responsible Officer: %s\n" +
                        "Start Date: %s\n" +
                        "%s" +
                        "Allocation Reason: %s\n",
                responsibleOfficerOf(offenderManager),
                offenderManager.getLatestResponsibleOfficer().getStartDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                Optional.ofNullable(offenderManager.getLatestResponsibleOfficer().getEndDateTime()).map(dateTime -> dateTime.format(DateTimeFormatter.ofPattern("'End Date: 'dd/MM/yyyy HH:mm:ss'\n'"))).orElse(""),
                offenderManager.getAllocationReason().getCodeDescription())
        ;
    }

    private String responsibleOfficerOf(final PrisonOffenderManager pom) {
        return Optional.ofNullable(pom.getStaff()).map(staff ->
                String.format("%s (%s, %s)", displayNameOf(staff), pom.getTeam().getDescription(), pom.getProbationArea().getDescription()))
                .orElse("");
    }
    private String responsibleOfficerOf(final OffenderManager com) {
        return Optional.ofNullable(com.getStaff()).map(staff ->
                String.format("%s (%s, %s)", displayNameOf(staff), com.getTeam().getDescription(), com.getProbationArea().getDescription()))
                .orElse("");
    }


    private String appendNoteForExistingPOMAllocation(final String notes, final PrisonOffenderManager existingPrisonOffenderManager) {
        return notes + Optional.ofNullable(existingPrisonOffenderManager.getTeam()).map(team ->
                "\n" +
                        Optional
                                .ofNullable(existingPrisonOffenderManager.getTeam().getProbationArea())
                                .map(probationArea -> String.format("From Establishment Provider: %s\n", existingPrisonOffenderManager.getTeam().getProbationArea().getDescription()))
                                .orElse("") +
                        String.format("From Team: %s\n", existingPrisonOffenderManager.getTeam().getDescription()) +
                        Optional
                                .ofNullable(existingPrisonOffenderManager.getStaff())
                                .map(staff -> String.format("From Officer: %s\n", displayNameOf(existingPrisonOffenderManager.getStaff())))
                        .orElse("")).orElse("");
    }

    private String displayNameOf(final Staff staff) {
        if (staff.isUnallocated()) {
            return "Unallocated";
        }

        if (staff.isInActive()) {
            return "Inactive";
        }

        return staff.getSurname() +
                ", " +
                staff.getForename() +
                " " +
                Optional.ofNullable(staff.getForname2()).orElse("");
    }

    private String notesForPrisonLocationChange(final Event event) {
        final var custody = event.getDisposal().getCustody();
        return String.format("%s%s%s-------------------------------",
                Optional.ofNullable(custody.getCustodialStatus()).map(status -> String.format("Custodial Status: %s\n", status.getCodeDescription())).orElse(""),
                Optional.ofNullable(custody.getInstitution()).map(institution -> String.format("Custodial Establishment: %s\n", institution.getDescription())).orElse(""),
                Optional.ofNullable(custody.getLocationChangeDate()).map(date -> String.format("Location Change Date: %s\n", date.format(DateTimeFormatter.ofPattern(DELIUS_DATE_FORMAT)))).orElse(""));
    }

    private String notesForBookingNumbUpdate(final Event event) {
        return String.format("Prison Number: %s\n", event.getDisposal().getCustody().getPrisonerNumber());
    }

    private void addContactForResponsibleOfficerChange(Long offenderId, String notes, Team team, Staff staff, ProbationArea probationArea) {
        final var contactType = contactTypeForResponsibleOfficerChange();
        contactRepository.save(builder()
                .contactDate(LocalDate.now())
                .contactStartTime(LocalTime.now())
                .offenderId(offenderId)
                .notes(notes)
                .team(team)
                .staff(staff)
                .probationArea(probationArea)
                .staffEmployeeId(staff.getStaffId())
                .teamProviderId(team.getTeamId())
                .contactType(contactType)
                .alertActive(contactType.getAlertFlag())
                .build());
    }

    public List<uk.gov.justice.digital.delius.data.api.ContactType> getAllContactTypes(final List<String> categories) {
        return (CollectionUtils.isEmpty(categories) ? contactTypeRepository.findAllBySelectableTrue()
        : contactTypeRepository.findAllByContactCategoriesCodeValueInAndSelectableTrue(categories))
            .stream().map(ContactTransformer::contactTypeOf).collect(toList());
    }
}
