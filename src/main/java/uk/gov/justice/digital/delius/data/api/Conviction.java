package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(name = "Unique id of this conviction", example = "2500000001")
    private Long convictionId;

    @Schema(name = "Index of this conviction", example = "1")
    private String index;

    @Schema(name = "Active conviction flag", example = "true")
    private Boolean active;

    @Schema(name = "Conviction in breach flag", example = "true")
    private Boolean inBreach;

    @Schema(name = "Total number of failure to comply outcomes since the last breach end", example = "3")
    private Long failureToComplyCount;

    @Schema(name = "Date of the last breach end, or null if no previous breach has occurred", example = "2021-05-13")
    private LocalDate breachEnd;

    @Schema(name = "Conviction is awaiting pre-sentence report", example = "true")
    private boolean awaitingPsr;

    @Schema(name = "Date of this conviction", example = "2021-06-10")
    private LocalDate convictionDate;

    @Schema(name = "Referral date of this conviction", example = "2021-06-10")
    private LocalDate referralDate;

    @Schema(name = "Main & additional offences that resulted in this conviction")
    private List<Offence> offences;

    @Schema(name = "Sentence given with this conviction")
    private Sentence sentence;

    @Schema(name = "Outcome of the latest court appearance for this conviction")
    private KeyValue latestCourtAppearanceOutcome;

    @Schema(name = "Custody details")
    private Custody custody;

    @Schema(name = "Court associated to this conviction")
    private Court responsibleCourt;

    @Schema(name = "Sentencing court appearance or the latest court appearance otherwise")
    private CourtAppearanceBasic courtAppearance;

    @Schema(name = "Offender manager supervising the order")
    private List<OrderManager> orderManagers;

}
