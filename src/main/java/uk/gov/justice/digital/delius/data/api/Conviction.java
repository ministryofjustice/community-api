package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class Conviction {
    private Long convictionId;
    private Long index;
    private Boolean active;
    private Boolean inBreach;
    private LocalDate convictionDate;
    private LocalDate referralDate;
    private List<Offence> offences;
    private Sentence sentence;
    private KeyValue latestCourtAppearanceOutcome;
}
