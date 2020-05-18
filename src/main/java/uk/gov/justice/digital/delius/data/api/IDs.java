package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class IDs {
    @ApiModelProperty(value = "case reference number", required = true, example = "12345C")
    private String crn;
    @ApiModelProperty(value = "Number from the police national computer", example = "2004/0712343H")
    private String pncNumber;
    @ApiModelProperty(value = "Number from the crime records office", example = "123456/04A")
    private String croNumber;
    @ApiModelProperty(value = "National insurance number from HMRC", example = "AA112233B")
    private String niNumber;
    @ApiModelProperty(value = "Offender number from NOMIS", example = "A1234CR")
    private String nomsNumber;
    @ApiModelProperty(value = "Immigration number", example = "A1234567")
    private String immigrationNumber;
    @ApiModelProperty(value = "Book number of latest booking from NOMIS", example = "G12345")
    private String mostRecentPrisonerNumber;
}
