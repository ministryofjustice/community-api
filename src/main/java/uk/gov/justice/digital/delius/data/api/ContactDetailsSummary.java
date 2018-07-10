package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ContactDetailsSummary {
    private List<PhoneNumber> phoneNumbers;
    private List<String> emailAddresses;
    private Boolean allowSMS;
}
