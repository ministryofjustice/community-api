package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OffenderLatestRecall {
    @ApiModelProperty(value = "Last recall")
    private OffenderRecall lastRecall;
    @ApiModelProperty(value = "Last release")
    private OffenderRelease lastRelease;
}
