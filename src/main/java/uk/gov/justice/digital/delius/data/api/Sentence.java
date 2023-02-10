package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sentence {
    private Long sentenceId;
    private String description;
    private Long originalLength;
    private String originalLengthUnits;
    private Long secondLength;
    private String secondLengthUnits;
    private Long defaultLength;
    private Long effectiveLength;
    private Long lengthInDays;
    @Schema(description = "The expected end date of the sentence")
    private LocalDate expectedSentenceEndDate;
    @Schema(description = "Unpaid Work to date associated with this sentence")
    private UnpaidWork unpaidWork;
    @Schema(description = "Date sentence started")
    private LocalDate startDate;
    private LocalDate terminationDate;
    private String terminationReason;
    @Schema(description = "Sentence type and description")
    private KeyValue sentenceType;

    @Schema(name = "Additional sentences if present")
    private List<AdditionalSentence> additionalSentences;

    @Schema(name = "Maximum number of appointments with a failure to comply outcome before a breach should be initiated")
    private Long failureToComplyLimit;

    @Schema(name = "A CJA 2003 or later order")
    private Boolean cja2003Order;

    @Schema(name = "A legacy order (pre-CJA 2003)")
    private Boolean  legacyOrder;
}
