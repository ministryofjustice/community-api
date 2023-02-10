package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDetail {
    @NotNull
    @Schema(name = "Appointment id", example = "1")
    private Long appointmentId;

    @NotNull
    @Schema(name = "Appointment start date & time", example = "2021-05-25T10:00:00+01:00")
    private OffsetDateTime appointmentStart;

    @NotNull
    @Schema(name = "Appointment end date & time", example = "2021-05-25T11:00:00+01:00")
    private OffsetDateTime appointmentEnd;

    @NotNull
    private AppointmentType type;

    @NotNull
    private OfficeLocation officeLocation;

    @NotNull
    @Schema(name = "Appointment notes", example = "Some interesting notes about the appointment.")
    private String notes;

    @NotNull
    @Schema(name = "Provider")
    private KeyValue provider;

    @NotNull
    @Schema(name = "Team")
    private KeyValue team;

    @NotNull
    @Schema(name = "Staff")
    private StaffHuman staff;

    @Schema(name = "Sensitive appointment flag", example = "true")
    private Boolean sensitive;

    @Schema(name = "Outcome")
    private AppointmentOutcome outcome;

    @Schema(name = "RAR activity flag", example = "true")
    private Boolean rarActivity;

    @Schema(name = "The related requirement if present")
    private AppointmentRequirementDetail requirement;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppointmentRequirementDetail {
        @Schema(name = "The unique identifier for an associated requirement", example = "25000000")
        private Long requirementId;

        @Schema(name = "The requirement is a RAR requirement", example = "true")
        private Boolean isRar;

        @Schema(name = "The requirement is active", example = "true")
        private Boolean isActive;
    }
}
