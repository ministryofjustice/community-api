package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffenderManagerGrade {
    @ApiModelProperty(value = "Grade code", example = "M")
    private String code;
    @ApiModelProperty(value = "Grade description", example = "PO")
    private String description;
}
