package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Court {
    private Long courtId;
    private String name;
    private String localJusticeArea;
}
