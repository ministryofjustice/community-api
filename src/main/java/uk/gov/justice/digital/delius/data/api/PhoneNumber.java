package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.util.Optional;

@Data
@Builder
public class PhoneNumber {
    private PhoneTypes type;
    private Optional<String> number;

    public enum PhoneTypes {TELEPHONE, MOBILE}
}
