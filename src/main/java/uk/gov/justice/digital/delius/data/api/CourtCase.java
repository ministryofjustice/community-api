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
public class CourtCase {
    private CourtAppearance courtAppearance;
    private CourtAppearance nextAppearance;
    private List<Offence> offences;
    private LocalDate referralDate;
    private LocalDate convictionDate;
    private OrderManager orderManager;
}
