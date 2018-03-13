package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Team {
    private String description;
    private String telephone;
    private KeyValue district;
    private KeyValue borough;
}
