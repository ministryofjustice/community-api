package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class Conviction {
    private Long convictionId;
    private Boolean active;
    private LocalDate convictionDate;
    private List<Offence> offences;
}
