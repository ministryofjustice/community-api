package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static org.apache.commons.text.WordUtils.capitalizeFully;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Human {
    @ApiModelProperty(value = "Given names", example = "Sheila Linda")    
    private String forenames;
    @ApiModelProperty(value = "Family name", example = "Hancock")    
    private String surname;

    public Human capitalise() {
        return this.toBuilder()
                .surname(capitalizeFully(surname))
                .forenames(capitalizeFully(forenames))
                .build();
    }
}
