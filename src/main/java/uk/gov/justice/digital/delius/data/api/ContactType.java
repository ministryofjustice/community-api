package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.util.Optional;

@Data
@Builder
public class ContactType {
    private String code;
    private String description;
    private Optional<String> shortDescription;
}
