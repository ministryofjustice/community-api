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
public class Human {
    @Schema(description = "Given names", example = "Sheila Linda")    
    private String forenames;
    @Schema(description = "Family name", example = "Hancock")    
    private String surname;

    public Human capitalise() {
        return this.toBuilder()
                .surname(capitalizeFully(surname))
                .forenames(capitalizeFully(forenames))
                .build();
    }
}
