package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder(toBuilder = true)
public class PreviousConviction {
    private LocalDate convictionDate;
    private Map<String, String> detail;
}
