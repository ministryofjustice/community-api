package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import lombok.Data;
import uk.gov.justice.digital.delius.data.api.views.Views;

import java.util.List;

@Data
@Builder
public class ContactDetails {
    private List<PhoneNumber> phoneNumbers;
    private List<String> emailAddresses;
    private Boolean allowSMS;
    @JsonView(Views.FullFat.class)
    private List<Address> addresses;
}
