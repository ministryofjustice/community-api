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
/* Convenience DTO to encompass ProviderTeam and Team entities and their FK deps */
public class AllTeam {
    @ApiModelProperty(required = true)
    private Long providerTeamId;
    @ApiModelProperty(required = true)
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
