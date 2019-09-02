package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class CourtCase {
    private CourtAppearance courtAppearance;
    private CourtAppearance nextAppearance;
    private List<Offence> offences;
    private LocalDate referralDate;
    private LocalDate convictionDate;
    private OrderManager orderManager;
}
