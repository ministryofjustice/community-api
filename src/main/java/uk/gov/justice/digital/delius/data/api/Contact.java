package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @ApiModelProperty(example = "12:00:00", dataType = "String")
    private LocalTime contactStartTime;
    @ApiModelProperty(example = "13:00:00", dataType = "String")
    private LocalTime contactEndTime;
    private Boolean softDeleted;
    private Boolean alertActive;
    private LocalDateTime createdDateTime;
    private LocalDateTime lastUpdatedDateTime;
    private KeyValue contactOutcomeType;
    private String partitionArea;
    private KeyValue probationArea;
    private KeyValue providerLocation;
    private KeyValue providerTeam;
    private KeyValue team;
    private StaffHuman staff;
    private Human providerEmployee;
    private Double hoursCredited;
    private Boolean visorContact;
    private Boolean attended;
    private Boolean complied;
    private Boolean documentLinked;
    private Boolean uploadLinked;

}
