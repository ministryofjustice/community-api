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
public class ContactableHuman {
    @ApiModelProperty(value = "Given names", example = "Sheila Linda")    
    private String forenames;
    @ApiModelProperty(value = "Family name", example = "Hancock")    
    private String surname;
    @ApiModelProperty(value = "Email address", example = "officer@gov.uk")
    private String email;
    @ApiModelProperty(value = "Phone number", example = "0123411278")
    private String phoneNumber;

    public ContactableHuman capitalise() {
        return this.toBuilder()
                .surname(capitalizeFully(surname))
                .forenames(capitalizeFully(forenames))
                .build();
    }
}
