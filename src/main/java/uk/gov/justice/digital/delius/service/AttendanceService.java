package uk.gov.justice.digital.delius.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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

    public List<Contact> getContactsForEvent(final Long offenderId, final Long eventId, final LocalDate localDate) {
        return contactRepository.findByOffenderAndEventIdEnforcement(offenderId, eventId, localDate);
    }

    public List<Contact> getContactsForEvent(final Long offenderId,
                                            final Long eventId,
                                            final LocalDate localDate,
                                            final String enforcement,
                                            final String attendanceContact,
                                            final String nationalStandardsContact) {
        return contactRepository.findByOffenderAndEventId(offenderId, eventId, localDate, enforcement, attendanceContact, nationalStandardsContact);
    }

    static boolean forEntityBoolean(final String booleanStr) {
        if (booleanStr == null)
            return false;
        return booleanStr.trim().equals("1");
    }

    public static List<Attendance> attendancesFor(final List<Contact> contacts) {

        if (contacts == null) {
            return Collections.emptyList();
        }

        return contacts
            .stream()
            .filter(Objects::nonNull)
            .map(contactEntity -> Attendance.builder()
                .attended(forEntityBoolean(contactEntity.getAttended()))
                .complied(forEntityBoolean(contactEntity.getComplied()))
                .attendanceDate(contactEntity.getContactDate())
                .contactId(contactEntity.getContactId())
                .outcome(contactEntity.getContactOutcomeType() != null ? contactEntity.getContactOutcomeType().getDescription() : null)
                .contactType(ContactTypeDetail.builder()
                    .code(contactEntity.getContactType() != null ? contactEntity.getContactType().getCode() : null)
                    .description(contactEntity.getContactType() != null ? contactEntity.getContactType().getDescription(): null)
                    .build())
                .build())
            .collect(Collectors.toList());
    }
}
