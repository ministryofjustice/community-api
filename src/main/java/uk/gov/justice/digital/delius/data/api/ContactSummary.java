package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactSummary {
    @NotNull
    @ApiModelProperty(name = "Contact id", required = true)
    private Long contactId;

    @ApiModelProperty(name = "Contact start date & time", example = "2021-05-25T10:00:00+01:00")
    private OffsetDateTime contactStart;

    @ApiModelProperty(name = "Contact end date & time", example = "2021-05-25T11:00:00+01:00")
    private OffsetDateTime contactEnd;

    @NotNull
    @ApiModelProperty(name = "Contact type", required = true)
    private ContactType type;

    @ApiModelProperty(name = "Office location")
    private OfficeLocation officeLocation;

    @ApiModelProperty(name = "Notes")
    private String notes;

    @NotNull
    @ApiModelProperty(name = "Provider", required = true)
    private KeyValue provider;

    @NotNull
    @ApiModelProperty(name = "Team", required = true)
    private KeyValue team;

    @NotNull
    @ApiModelProperty(name = "Staff", required = true)
    private StaffHuman staff;

    @ApiModelProperty(name = "Sensitive contact flag", example = "true")
    private Boolean sensitive;

    @ApiModelProperty(name = "Outcome")
    private AppointmentOutcome outcome;

    @ApiModelProperty(name = "RAR activity flag", example = "true")
    private Boolean rarActivity;
}
