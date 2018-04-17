package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.Contact;
import uk.gov.justice.digital.delius.jpa.filters.ContactFilter;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.transformers.ContactTransformer;

import java.util.List;

@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final ContactTransformer contactTransformer;

    @Autowired
    public ContactService(ContactRepository contactRepository, ContactTransformer contactTransformer) {
        this.contactRepository = contactRepository;
        this.contactTransformer = contactTransformer;
    }

    public List<Contact> contactsFor(Long offenderId, ContactFilter filter) {
        return contactTransformer.contactsOf(contactRepository.findAll(filter.toBuilder().offenderId(offenderId).build()));
    }

}
