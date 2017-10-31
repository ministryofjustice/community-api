package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Data
@Builder(toBuilder = true)
public class Conviction {
    private Optional<LocalDate> convictionDate;
    private Map<String, String> detail;
}
