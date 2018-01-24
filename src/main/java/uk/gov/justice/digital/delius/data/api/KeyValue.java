package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KeyValue {
    private String code;
    private String description;
}
