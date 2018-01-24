package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.ContactReport;
import uk.gov.justice.digital.delius.jpa.entity.Contact;
import uk.gov.justice.digital.delius.jpa.entity.Event;
import uk.gov.justice.digital.delius.jpa.repository.ContactRepository;
import uk.gov.justice.digital.delius.transformers.ContactTransformer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final ContactTransformer contactTransformer;

    @Autowired
    public ContactService(ContactRepository contactRepository, ContactTransformer contactTransformer) {
        this.contactRepository = contactRepository;
        this.contactTransformer = contactTransformer;
    }

    public Optional<ContactReport> contactReportFor(Long offenderId) {
        List<Contact> contacts = contactRepository.findByOffenderId(offenderId);

        if (contacts.isEmpty()) {
            return Optional.empty();
        }

        List<Contact> contactsWithoutEvent = contacts.stream().filter(c -> c.getEvent() == null).collect(Collectors.toList());

        List<Contact> contactsWithEvent = contacts.stream().filter(c -> c.getEvent() != null).collect(Collectors.toList());


        Map<Event, List<Contact>> contactsGroupedByEvent = contactsWithEvent.stream().collect(Collectors.groupingBy(Contact::getEvent));

        List<uk.gov.justice.digital.delius.data.api.Event> events = contactsGroupedByEvent.entrySet().stream().map(entry ->
                uk.gov.justice.digital.delius.data.api.Event.builder()
                        .active(entry.getKey().getActiveFlag() == 1)
                        .inBreach(entry.getKey().getInBreach() == 1)
                        .notes(Optional.ofNullable(entry.getKey().getNotes()))
                        .contacts(contactTransformer.contactsOf(entry.getValue()))
                        .build()).collect(Collectors.toList());


        List<uk.gov.justice.digital.delius.data.api.Contact> eventlessContacts = contactsWithoutEvent.stream().map(contactTransformer::contactOf).collect(Collectors.toList());

        return Optional.of(ContactReport.builder()
                .events(events)
                .eventlessContacts(eventlessContacts)
                .build());

    }

}
