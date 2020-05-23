package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.data.api.Contact;
import uk.gov.justice.digital.delius.jpa.filters.ContactFilter;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;
import uk.gov.justice.digital.delius.transformers.ContactTransformer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.justice.digital.delius.jpa.standard.entity.Contact.*;

@Service
public class ContactService {

    private static final String PRISONER_OFFENDER_MANAGER_ALLOCATION_CONTACT_TYPE = "EPOMAT";
    private static final String PRISONER_OFFENDER_MANAGER_INTERNAL_ALLOCATION_CONTACT_TYPE = "EPOMIN";
    private static final String PRISONER_OFFENDER_MANAGER_EXTERNAL_ALLOCATION_CONTACT_TYPE = "EPOMEX";
    private static final String RESPONSIBLE_OFFICER_CHANGE_CONTACT_TYPE = "ROC";
    private static final String PRISON_LOCATION_CHANGE_CONTACT_TYPE = "ETCP";
    private static final String CUSTODY_AUTO_UPDATE_CONTACT_TYPE = "EDSS";
    private final ContactRepository contactRepository;
    private final ContactTypeRepository contactTypeRepository;

    @Autowired
    public ContactService(ContactRepository contactRepository, ContactTypeRepository contactTypeRepository) {
        this.contactRepository = contactRepository;
        this.contactTypeRepository = contactTypeRepository;
    }

    public List<Contact> contactsFor(Long offenderId, ContactFilter filter) {
        return ContactTransformer.contactsOf(contactRepository.findAll(filter.toBuilder().offenderId(offenderId).build()));
    }


    @Transactional
    public void addContactForPOMAllocation(PrisonOffenderManager newPrisonOffenderManager) {
        contactRepository.save(contactForPOMAllocation(newPrisonOffenderManager));
    }

    @Transactional
    public void addContactForPOMAllocation(PrisonOffenderManager newPrisonOffenderManager, PrisonOffenderManager existingPrisonOffenderManager) {
        var contact = contactForPOMAllocation(newPrisonOffenderManager);

        contactRepository.save(contact
                .toBuilder()
                .notes(appendNoteForExistingPOMAllocation(contact.getNotes(), existingPrisonOffenderManager))
                .build());
    }

    @Transactional
    public void addContactForResponsibleOfficerChange(PrisonOffenderManager newPrisonOffenderManager, PrisonOffenderManager existingPrisonOffenderManager) {
        final ContactType contactType = contactTypeForResponsibleOfficerChange();
        contactRepository.save(builder()
                .contactDate(newPrisonOffenderManager.getAllocationDate())
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
    public void addContactForPrisonLocationChange(Offender offender, Event event) {
        addContactForCustodyChange(offender,
                event,
                contactTypeForPrisonLocationChange(),
                notesForPrisonLocationChange(event));
    }


    @Transactional
    public void addContactForBookingNumberUpdate(Offender offender, Event event) {
        addContactForCustodyChange(offender,
                event,
                contactTypeForCustodyAutoUpdate(),
                notesForBookingNumbUpdate(event));
    }


    @Transactional
    public void addContactForBulkCustodyKeyDateUpdate(Offender offender, Event event, Map<String, LocalDate> datesAmendedOrUpdated, Map<String, LocalDate> datesRemoved) {
        addContactForCustodyChange(offender,
                event,
                contactTypeForCustodyAutoUpdate(),
                notesForKeyDatesUpdate(datesAmendedOrUpdated, datesRemoved));
    }

    private String notesForKeyDatesUpdate(Map<String, LocalDate> datesAmendedOrUpdated, Map<String, LocalDate> datesRemoved) {
        var notes = datesAmendedOrUpdated.entrySet().stream().map(entry -> String
                .format("%s: %s\n", entry.getKey(), entry.getValue()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))))
                .reduce("", (original, it) -> original + it);

        return datesRemoved.entrySet().stream().map(entry -> String
                .format("Removed %s: %s\n", entry.getKey(), entry.getValue()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))))
                .reduce(notes, (original, it) -> original + it);
    }

    private void addContactForCustodyChange(Offender offender, Event event, ContactType contactType, String notes) {
        final var mayBeOrderManager = event.getOrderManagers()
                .stream()
                .filter(OrderManager::isActive)
                .findFirst();

        contactRepository.save(builder()
                .contactDate(LocalDate.now())
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

    private uk.gov.justice.digital.delius.jpa.standard.entity.Contact contactForPOMAllocation(PrisonOffenderManager newPrisonOffenderManager) {
        final ContactType contactType = contactTypeForPOMAllocationOf(newPrisonOffenderManager.getAllocationReason());
        return builder()
                .contactDate(newPrisonOffenderManager.getAllocationDate())
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

    private ContactType contactTypeForPOMAllocationOf(StandardReference allocationReason) {
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

    private String notesForPOMAllocation(PrisonOffenderManager newPrisonOffenderManager) {
        return "Transfer Reason: " +
                newPrisonOffenderManager.getAllocationReason().getCodeDescription() +
                "\n" +
                "Transfer Date: " +
                newPrisonOffenderManager.getAllocationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                "\n";
    }

    private String notesForResponsibleManager(PrisonOffenderManager newPrisonOffenderManager, PrisonOffenderManager existingPrisonOffenderManager) {
        return String.format(
                "New Details:\n" +
                "%s\n" +
                "Previous Details:\n" +
                "%s\n",
                notesForResponsibleManagerOf(newPrisonOffenderManager),
                notesForResponsibleManagerOf(existingPrisonOffenderManager));
    }

    private String notesForResponsibleManagerOf(PrisonOffenderManager prisonOffenderManager) {
        return String.format(
                "Responsible Officer Type: Prison Offender Manager\n" +
                "Responsible Officer: %s\n" +
                "Start Date: %s\n" +
                "%s" +
                "Allocation Reason: %s\n",
                responsibleOfficerOf(prisonOffenderManager),
                prisonOffenderManager.getResponsibleOfficer().getStartDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                Optional.ofNullable(prisonOffenderManager.getResponsibleOfficer().getEndDateTime()).map(dateTime -> dateTime.format(DateTimeFormatter.ofPattern("'End Date: 'dd/MM/yyyy HH:mm:ss'\n'"))).orElse(""),
                prisonOffenderManager.getAllocationReason().getCodeDescription())
        ;
    }

    private String responsibleOfficerOf(PrisonOffenderManager pom) {
        return Optional.ofNullable(pom.getStaff()).map(staff ->
                String.format("%s (%s, %s)", displayNameOf(staff), pom.getTeam().getDescription(), pom.getProbationArea().getDescription()))
                .orElse("");
    }


    private String appendNoteForExistingPOMAllocation(String notes, PrisonOffenderManager existingPrisonOffenderManager) {
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

    private String displayNameOf(Staff staff) {
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

    private String notesForPrisonLocationChange(Event event) {
        final var custody = event.getDisposal().getCustody();
        return String.format("%s%s%s-------------------------------",
                Optional.ofNullable(custody.getCustodialStatus()).map(status -> String.format("Custodial Status: %s\n", status.getCodeDescription())).orElse(""),
                Optional.ofNullable(custody.getInstitution()).map(institution -> String.format("Custodial Establishment: %s\n", institution.getDescription())).orElse(""),
                Optional.ofNullable(custody.getLocationChangeDate()).map(date -> String.format("Location Change Date: %s\n", date.format(DateTimeFormatter.ISO_LOCAL_DATE))).orElse(""));
    }

    private String notesForBookingNumbUpdate(Event event) {
        return String.format("Prison Number: %s\n", event.getDisposal().getCustody().getPrisonerNumber());
    }
}
