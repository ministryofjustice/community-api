package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Contact {
    protected Long linkedContactId;
    @ApiModelProperty(required = true)
    private Long contactId;
    private Long eventId;
    @ApiModelProperty(required = true)
    private ContactType contactType;
    private Requirement requirement;
    private KeyValue explanation;
    private LicenceCondition licenceCondition;
    private Nsi nsi;
    private String notes;
    private LocalDateTime contactStartTime;
    private LocalDateTime contactEndTime;
    private boolean softDeleted;
    private boolean alertActive;
    private LocalDateTime createdDateTime;
    private LocalDateTime lastUpdatedDateTime;
    private KeyValue contactOutcomeType;
    private String partitionArea;
    private KeyValue probationArea;
    private KeyValue providerLocation;
    private KeyValue providerTeam;
    private KeyValue team;
    private Human staff;
    private Human providerEmployee;
    private Double hoursCredited;
    private Boolean visorContact;
    private Boolean attended;
    private Boolean complied;
    private Boolean documentLinked;
    private Boolean uploadLinked;

}
