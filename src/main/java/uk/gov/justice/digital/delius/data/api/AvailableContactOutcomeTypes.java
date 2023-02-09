package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableContactOutcomeTypes {
    @Schema(name = "Indicates if an outcome is required if the contact date is in the past", required = true)
    private RequiredOptional outcomeRequired;

    @Schema(name = "Outcomes available for this contact type", required = true)
    private List<ContactOutcomeTypeDetail> outcomeTypes;
}
