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
/* Convenience DTO to encompass ProviderTeam and Team entities and their FK deps */
public class AllTeam {
    @Schema(required = true)
    private Long providerTeamId;
    @Schema(required = true)
    private Long teamId;
    private String code;
    private String description;
    private String name;
    private Boolean isPrivate;
    private KeyValue externalProvider;
    private KeyValue scProvider;
    private KeyValue localDeliveryUnit;
    private KeyValue district;
    private KeyValue borough;
}
