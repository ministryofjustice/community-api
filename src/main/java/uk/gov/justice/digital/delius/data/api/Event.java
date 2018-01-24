package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
@Builder
public class Event {
    private Optional<String> notes;
    private boolean active;
    private boolean inBreach;
    private List<Contact> contacts;
}
