package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
@Builder
public class ContactDetails {
    private List<PhoneNumber> phoneNumbers;
    private List<String> emailAddresses;
    private Optional<Boolean> allowSMS;
}
