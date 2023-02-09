package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class IDs {
    @Schema(description = "case reference number", required = true, example = "12345C")
    private String crn;
    @Schema(description = "Number from the police national computer", example = "2004/0712343H")
    private String pncNumber;
    @Schema(description = "Number from the crime records office", example = "123456/04A")
    private String croNumber;
    @Schema(description = "National insurance number from HMRC", example = "AA112233B")
    private String niNumber;
    @Schema(description = "Offender number from NOMIS", example = "A1234CR")
    private String nomsNumber;
    @Schema(description = "Immigration number", example = "A1234567")
    private String immigrationNumber;
    @Schema(description = "Book number of latest booking from NOMIS", example = "G12345")
    private String mostRecentPrisonerNumber;
}
