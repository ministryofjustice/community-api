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
public class Event {
    @Schema(required = true)
    private Long eventId;
    private String notes;
    @Schema(required = true)
    private Boolean active;
    @Schema(required = true)
    private Boolean inBreach;
    private List<Contact> contacts;
}
