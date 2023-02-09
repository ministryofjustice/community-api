package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Institution {
    @Schema(required = true)
    private Long institutionId;
    private Boolean isEstablishment;
    private String code;
    private String description;
    private String institutionName;
    private KeyValue establishmentType;
    private Boolean isPrivate;
    @Schema(description = "Prison institution code in NOMIS")
    private String nomsPrisonInstitutionCode;
}
