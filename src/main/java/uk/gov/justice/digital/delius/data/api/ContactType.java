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
public class ContactType {
    @Schema(required = true)
    private String code;
    @Schema(required = true)
    private String description;
    private String shortDescription;

    @Schema(name = "Does this contact type represent an appointment type")
    private Boolean appointment;

    @Schema(name = "Does this contact type represent a national standard contact")
    private Boolean nationalStandard;

    @Schema(name = "Active categories this contact type belongs belongs to")
    private List<KeyValue> categories;

    @Schema(name = "Does this contact type represent a system generated type")
    private Boolean systemGenerated;
}
