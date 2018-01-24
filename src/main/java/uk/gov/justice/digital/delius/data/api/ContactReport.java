package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ContactReport {

    private List<Event> events;
    private List<Contact> eventlessContacts;

}
