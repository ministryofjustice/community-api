package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
/* Convenience DTO to encompass ProviderTeam and Team entities and their FK deps */
public class AllTeam {
    private Long providerTeamId;
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
