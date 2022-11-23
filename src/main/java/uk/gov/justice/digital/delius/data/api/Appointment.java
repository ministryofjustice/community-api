package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
    @JsonInclude
    protected Long linkedContactId;
    @ApiModelProperty(required = true)
    private Long appointmentId;
    private Long eventId;
    @ApiModelProperty(required = true)
    private KeyValue appointmentType;
    private Requirement requirement;
    private KeyValue explanation;
    private LicenceCondition licenceCondition;
    private Nsi nsi;
    private String notes;
    private LocalDate appointmentDate;
    private LocalTime appointmentStartTime;
    private LocalTime appointmentEndTime;
    private Boolean alertActive;
    private LocalDateTime createdDateTime;
    private LocalDateTime lastUpdatedDateTime;
    private KeyValue appointmentOutcomeType;
    private StaffHuman staff;
    private KeyValue team;
    private KeyValue officeLocation;
    private KeyValue probationArea;
    private KeyValue providerTeam;
    private KeyValue providerLocation;
    private Human providerEmployee;
    private Double hoursCredited;
    private Boolean visorContact;
    private Attended attended;
    private Boolean complied;
    private Boolean documentLinked;
    private Boolean uploadLinked;

    public enum Attended {
        ATTENDED,
        UNATTENDED,
        NOT_RECORDED
    }

}
