package uk.gov.justice.digital.delius.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.Attendance;
import uk.gov.justice.digital.delius.data.api.Attendance.ContactTypeDetail;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;

@Slf4j
@Service
public class AttendanceService {

    private final ContactRepository contactRepository;

    @Autowired
    public AttendanceService(final ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public Optional<List<Contact>> getContactsForEvent(final Long offenderId, final Long eventId, final LocalDate localDate) {
        final List<Contact> contacts = contactRepository.findByOffenderAndEventIdEnforcement(offenderId, eventId, localDate);
        return contacts.isEmpty() ? Optional.empty() : Optional.of(contacts);
    }

    static boolean forEntityBoolean(final String booleanStr) {
        if (booleanStr == null)
            return false;
        return booleanStr.trim().equals("1");
    }

    public static List<Attendance> attendancesFor(List<Contact> contacts) {

        return contacts
            .stream()
            .map(contactEntity -> Attendance.builder()
                .attended(forEntityBoolean(contactEntity.getAttended()))
                .complied(forEntityBoolean(contactEntity.getComplied()))
                .attendanceDate(contactEntity.getContactDate())
                .contactId(contactEntity.getContactId())
                .outcome(contactEntity.getContactOutcomeType() != null ? contactEntity.getContactOutcomeType().getDescription() : null)
                .contactType(ContactTypeDetail.builder()
                    .code(contactEntity.getContactType() != null ? contactEntity.getContactType().getCode() : "")
                    .description(contactEntity.getContactType() != null ? contactEntity.getContactType().getDescription(): "")
                    .build())
                .build())
            .collect(Collectors.toList());
    }
}
