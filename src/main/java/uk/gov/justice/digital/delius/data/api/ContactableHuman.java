package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "Given names", example = "Sheila Linda")    
    private String forenames;
    @Schema(description = "Family name", example = "Hancock")    
    private String surname;
    @Schema(description = "Email address", example = "officer@gov.uk")
    private String email;
    @Schema(description = "Phone number", example = "0123411278")
    private String phoneNumber;

    public ContactableHuman capitalise() {
        return this.toBuilder()
                .surname(capitalizeFully(surname))
                .forenames(capitalizeFully(forenames))
                .build();
    }
}
