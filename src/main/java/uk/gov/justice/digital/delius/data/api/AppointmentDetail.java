package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDetail {
    @NotNull
    @ApiModelProperty(name = "Appointment id", example = "1")
    private Long appointmentId;

    @NotNull
    @ApiModelProperty(name = "Appointment start date & time", example = "2021-05-25T10:00:00+01:00")
    private OffsetDateTime appointmentStart;

    @NotNull
    @ApiModelProperty(name = "Appointment end date & time", example = "2021-05-25T11:00:00+01:00")
    private OffsetDateTime appointmentEnd;

    @NotNull
    private AppointmentType type;

    @NotNull
    private OfficeLocation officeLocation;

    @NotNull
    @ApiModelProperty(name = "Appointment notes", example = "Some interesting notes about the appointment.")
    private String notes;

    @NotNull
    @ApiModelProperty(name = "Provider")
    private KeyValue provider;

    @NotNull
    @ApiModelProperty(name = "Team")
    private KeyValue team;

    @NotNull
    @ApiModelProperty(name = "Staff")
    private StaffHuman staff;

    @ApiModelProperty(name = "Sensitive appointment flag", example = "true")
    private Boolean sensitive;

    @ApiModelProperty(name = "Outcome")
    private AppointmentOutcome outcome;

    @ApiModelProperty(name = "RAR activity flag", example = "true")
    private Boolean rarActivity;

    @ApiModelProperty(name = "The related requirement if present")
    private AppointmentRequirementDetail requirement;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppointmentRequirementDetail {
        @ApiModelProperty(name = "The unique identifier for an associated requirement", example = "25000000")
        private Long requirementId;

        @ApiModelProperty(name = "The requirement is a RAR requirement", example = "true")
        private Boolean isRar;

        @ApiModelProperty(name = "The requirement is active", example = "true")
        private Boolean isActive;
    }
}
