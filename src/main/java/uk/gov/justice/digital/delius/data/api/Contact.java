package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Optional;

@Data
@Builder
public class Contact {
    protected Optional<Long> linkedContactId;
    @ApiModelProperty(required = true)
    private Long contactId;
    private Optional<Long> eventId;
    @ApiModelProperty(required = true)
    private ContactType contactType;
    private Optional<Requirement> requirement;
    private Optional<KeyValue> explanation;
    private Optional<LicenceCondition> licenceCondition;
    private Optional<Nsi> nsi;
    private Optional<String> notes;
    private LocalDateTime contactStartTime;
    private LocalDateTime contactEndTime;
    private boolean softDeleted;
    private boolean alertActive;
    private LocalDateTime createdDateTime;
    private LocalDateTime lastUpdatedDateTime;
    private Optional<KeyValue> contactOutcomeType;
    private Optional<String> partitionArea;
    private Optional<KeyValue> probationArea;
    private Optional<KeyValue> providerLocation;
    private Optional<KeyValue> providerTeam;
    private Optional<KeyValue> team;
    private Optional<Human> staff;
    private Optional<Human> providerEmployee;
    private Optional<Double> hoursCredited;
    private Optional<Boolean> visorContact;
    private Optional<Boolean> attended;
    private Optional<Boolean> complied;
    private Optional<Boolean> documentLinked;
    private Optional<Boolean> uploadLinked;

}
