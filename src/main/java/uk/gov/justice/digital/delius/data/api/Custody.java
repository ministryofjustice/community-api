package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Custody {
    private String bookingNumber;
    private Institution institution;
}
