package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Event {
    @ApiModelProperty(required = true)
    private Long eventId;
    private String notes;
    @ApiModelProperty(required = true)
    private Boolean active;
    @ApiModelProperty(required = true)
    private Boolean inBreach;
    private List<Contact> contacts;
}
