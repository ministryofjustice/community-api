package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
@Builder
public class Event {
    @ApiModelProperty(required = true)
    private Long eventId;
    private Optional<String> notes;
    @ApiModelProperty(required = true)
    private boolean active;
    @ApiModelProperty(required = true)
    private boolean inBreach;
    private List<Contact> contacts;
}
