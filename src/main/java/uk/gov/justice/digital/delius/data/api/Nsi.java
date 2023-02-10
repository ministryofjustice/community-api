package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
public class Nsi {
    private Long nsiId;
    private KeyValue nsiType;
    private KeyValue nsiSubType;
    private KeyValue nsiOutcome;
    private Requirement requirement;
    private KeyValue nsiStatus;
    private LocalDateTime statusDateTime;
    private LocalDate actualStartDate;
    private LocalDate expectedStartDate;
    private LocalDate actualEndDate;
    private LocalDate expectedEndDate;
    private LocalDate referralDate;
    private Long length;
    private String lengthUnit;
    private List<NsiManager> nsiManagers;
    private String notes;
    private ProbationArea intendedProvider;
    private Boolean active;
    private Boolean softDeleted;
    private String externalReference;

    static <E extends Enum<E>> E valueOf(E defaultValue, String code) {
        try {
            return Enum.valueOf(defaultValue.getDeclaringClass(), code);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Schema(description = "present only for recalls, convenience property indicating this resulted in a recall")
    public Boolean isOutcomeRecall() {
        return Optional.ofNullable(asOutcomeType()).map(OutcomeType::getIsOutcomeRecall).orElse(null);
    }

    @Schema(description = "present only for recalls, convenience property indicating the recall was never accepted")
    public Boolean isRecallRejectedOrWithdrawn() {
        return Optional.ofNullable(asStatus())
            .map(Status::getIsRejectedOrWithdrawn)
            .map(rejectedOrWithdrawn -> {
                if (rejectedOrWithdrawn) {
                    return true;
                } else {
                    return Optional
                        .ofNullable(asOutcomeType())
                        .map(OutcomeType::getIsOutcomeRejectedOrWithdrawn)
                        .orElse(false);
                }
            }).orElse(null);
    }

    private OutcomeType asOutcomeType() {
        return Optional
            .ofNullable(nsiOutcome)
            .map(outcome -> valueOf(OutcomeType.UNKNOWN, outcome.getCode()))
            .orElse(null);
    }

    private Status asStatus() {
        return Optional.ofNullable(nsiStatus).map(status -> valueOf(Status.UNKNOWN, status.getCode())).orElse(null);
    }

    @Getter
    enum OutcomeType {
        REC01("Fixed Term Recall", true, false),
        REC02("Standard Term Recall", true, false),
        REC03("Recall Rejected by NPS", false, true),
        REC04("Recall Rejected by PPCS", false, true),
        REC05("Request Withdrawn by OM", false, true),
        UNKNOWN("No recall matching code", null, null);

        private final String description;
        private final Boolean isOutcomeRejectedOrWithdrawn;
        private final Boolean isOutcomeRecall;

        OutcomeType(String description, Boolean isOutcomeRecall, Boolean isOutcomeRejectedOrWithdrawn) {
            this.description = description;
            this.isOutcomeRecall = isOutcomeRecall;
            this.isOutcomeRejectedOrWithdrawn = isOutcomeRejectedOrWithdrawn;
        }
    }

    @Getter
    enum Status {
        REC01("Recall Initiated", false),
        REC02("Part A Completed by NPS/CRC OM", false),
        REC03("Part A Countersigned by NPS/CRC Manager", false),
        REC04("NPS Recall Endorsed by Senior Manager", false),
        REC05("NPS Recall Rejected by Senior Manager", true),
        REC06("Recall Referred to NPS", false),
        REC07("PPCS Recall Decision Received", false),
        REC08("Recall Submitted to PPCS", false),
        REC09("Out-of-hours Recall Instigated", false),
        REC10("Request Withdrawn by OM", true),
        UNKNOWN("No recall matching code", null);

        private final String description;
        private final Boolean isRejectedOrWithdrawn;

        Status(String description, Boolean isRejectedOrWithdrawn) {
            this.description = description;
            this.isRejectedOrWithdrawn = isRejectedOrWithdrawn;
        }
    }
}

