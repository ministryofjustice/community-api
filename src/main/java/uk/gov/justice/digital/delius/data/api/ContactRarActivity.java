package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactRarActivity {
    @NotNull
    @ApiModelProperty(name = "The ID of the RAR requirement")
    private Long requirementId;

    @ApiModelProperty(name = "The ID of the RAR NSI if present")
    private Long nsiId;

    @ApiModelProperty(name = "The RAR type if known")
    private KeyValue type;

    @ApiModelProperty(name = "The RAR subtype if known")
    private KeyValue subtype;
}
