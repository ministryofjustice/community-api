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
public class KeyValue {
    @ApiModelProperty(name = "Code", example = "ABC123", position = 1)
    private String code;

    @ApiModelProperty(name = "Description", example = "Some description", position = 2)
    private String description;
}
