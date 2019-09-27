package uk.gov.justice.digital.delius.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactDetailsSummary {
    private List<PhoneNumber> phoneNumbers;
    private List<String> emailAddresses;
    private Boolean allowSMS;
}
