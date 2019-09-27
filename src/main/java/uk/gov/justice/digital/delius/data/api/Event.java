package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(required = true)
    private Long eventId;
    private String notes;
    @ApiModelProperty(required = true)
    private Boolean active;
    @ApiModelProperty(required = true)
    private Boolean inBreach;
    private List<Contact> contacts;
}
