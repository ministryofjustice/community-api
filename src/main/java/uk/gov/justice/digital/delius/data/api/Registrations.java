package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Registration Wrapper")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Registrations {
    @Schema(description = "List of registrations")
    private List<Registration> registrations;
}
