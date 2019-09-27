package uk.gov.justice.digital.delius.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Conviction {
    private Long convictionId;
    private String index;
    private Boolean active;
    private Boolean inBreach;
    private LocalDate convictionDate;
    private LocalDate referralDate;
    private List<Offence> offences;
    private Sentence sentence;
    private KeyValue latestCourtAppearanceOutcome;
    private Custody custody;
}
