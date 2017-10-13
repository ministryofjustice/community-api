package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PhoneNumber {
    private PhoneTypes type;
    private String number;

    public enum PhoneTypes {TELEPHONE, MOBILE}
}
