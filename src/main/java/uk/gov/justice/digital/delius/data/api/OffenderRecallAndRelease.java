package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OffenderRecallAndRelease {
    @ApiModelProperty(value = "replace me", example = "replace me")
    private String replaceMe;
}
