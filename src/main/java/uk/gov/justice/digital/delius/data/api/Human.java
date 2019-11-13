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
public class Human {
    @ApiModelProperty(value = "Given names", example = "Sheila Linda")    
    private String forenames;
    @ApiModelProperty(value = "Family name", example = "Hancock")    
    private String surname;
}
