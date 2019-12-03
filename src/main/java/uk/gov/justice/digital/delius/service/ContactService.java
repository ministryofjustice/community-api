package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.data.api.Contact;
import uk.gov.justice.digital.delius.jpa.filters.ContactFilter;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;
import uk.gov.justice.digital.delius.jpa.standard.entity.PrisonOffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;
import uk.gov.justice.digital.delius.transformers.ContactTransformer;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class ContactService {

    private static final String PRISONER_OFFENDER_MANAGER_ALLOCATION_CONTACT_TYPE = "EPOMAT";
    private static final String PRISONER_OFFENDER_MANAGER_INTERNAL_ALLOCATION_CONTACT_TYPE = "EPOMIN";
    private static final String PRISONER_OFFENDER_MANAGER_EXTERNAL_ALLOCATION_CONTACT_TYPE = "EPOMEX";
    private final ContactRepository contactRepository;
    private final ContactTypeRepository contactTypeRepository;
    private final ContactTransformer contactTransformer;

    @Autowired
    public ContactService(ContactRepository contactRepository, ContactTypeRepository contactTypeRepository, ContactTransformer contactTransformer) {
        this.contactRepository = contactRepository;
        this.contactTypeRepository = contactTypeRepository;
        this.contactTransformer = contactTransformer;
    }

    public List<Contact> contactsFor(Long offenderId, ContactFilter filter) {
        return contactTransformer.contactsOf(contactRepository.findAll(filter.toBuilder().offenderId(offenderId).build()));
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

    private uk.gov.justice.digital.delius.jpa.standard.entity.Contact contactForPOMAllocation(PrisonOffenderManager newPrisonOffenderManager) {
        final ContactType contactType = contactTypeForPOMAllocationOf(newPrisonOffenderManager.getAllocationReason());
        return uk.gov.justice.digital.delius.jpa.standard.entity.Contact
                .builder()
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

    private String notesForPOMAllocation(PrisonOffenderManager newPrisonOffenderManager) {
        return "Transfer Reason: " +
                newPrisonOffenderManager.getAllocationReason().getCodeDescription() +
                "\n" +
                "Transfer Date: " +
                newPrisonOffenderManager.getAllocationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                "\n";
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
}
