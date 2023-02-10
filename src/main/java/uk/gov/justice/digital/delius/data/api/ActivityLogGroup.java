package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogGroup {
    @NotNull
    @Schema(name = "Date of all entries in this activity log", example = "2021-05-25")
    private LocalDate date;

    @Schema(name = "Counted in the RAR day calculation", example = "true")
    private Boolean rarDay;

    @NotNull
    @Schema(name = "Entries in this activity log group")
    private List<ActivityLogEntry> entries;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityLogEntry {
        @NotNull
        @Schema(name = "Contact id", required = true)
        private Long contactId;

        @Schema(name = "Conviction ID if present")
        private Long convictionId;

        @NotNull
        @Schema(name = "Start time of this activity log entry", example = "12:00:00+01:00")
        private LocalTime startTime;

        @Schema(name = "End time of this activity log entry", example = "13:00:00+01:00")
        private LocalTime endTime;

        @NotNull
        @Schema(name = "Contact type", required = true)
        private ContactType type;

        @Schema(name = "Notes")
        private String notes;

        @NotNull
        @Schema(name = "Staff", required = true)
        private StaffHuman staff;

        @Schema(name = "Sensitive contact flag", example = "true")
        private Boolean sensitive;

        @Schema(name = "Outcome")
        private AppointmentOutcome outcome;

        @Schema(name = "RAR details, otherwise, this activity log entry is not counted in the RAR days calculation")
        private ContactRarActivity rarActivity;

        @Schema(name = "Date time when contact was last updated", example = "2021-05-25T10:00:00+01:00")
        private OffsetDateTime lastUpdatedDateTime;

        @Schema(name = "Details of the person last updated the contact")
        private Human lastUpdatedByUser;

        @Schema(name = "Enforcement details of this contact")
        private Enforcement enforcement;
    }
}
