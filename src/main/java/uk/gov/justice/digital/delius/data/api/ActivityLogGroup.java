package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
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
    @ApiModelProperty(name = "Date of all entries in this activity log", example = "2021-05-25")
    private LocalDate date;

    @ApiModelProperty(name = "Counted in the RAR day calculation", example = "true")
    private Boolean rarDay;

    @NotNull
    @ApiModelProperty(name = "Entries in this activity log group")
    private List<ActivityLogEntry> entries;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityLogEntry {
        @NotNull
        @ApiModelProperty(name = "Contact id", required = true)
        private Long contactId;

        @ApiModelProperty(name = "Conviction ID if present")
        private Long convictionId;

        @NotNull
        @ApiModelProperty(name = "Start time of this activity log entry", example = "12:00:00+01:00")
        private LocalTime startTime;

        @ApiModelProperty(name = "End time of this activity log entry", example = "13:00:00+01:00")
        private LocalTime endTime;

        @NotNull
        @ApiModelProperty(name = "Contact type", required = true)
        private ContactType type;

        @ApiModelProperty(name = "Notes")
        private String notes;

        @NotNull
        @ApiModelProperty(name = "Staff", required = true)
        private StaffHuman staff;

        @ApiModelProperty(name = "Sensitive contact flag", example = "true")
        private Boolean sensitive;

        @ApiModelProperty(name = "Outcome")
        private AppointmentOutcome outcome;

        @ApiModelProperty(name = "RAR details, otherwise, this activity log entry is not counted in the RAR days calculation")
        private ContactRarActivity rarActivity;

        @ApiModelProperty(name = "Date time when contact was last updated", example = "2021-05-25T10:00:00+01:00")
        private OffsetDateTime lastUpdatedDateTime;

        @ApiModelProperty(name = "Details of the person last updated the contact")
        private Human lastUpdatedByUser;

        @ApiModelProperty(name = "Enforcement details of this contact")
        private Enforcement enforcement;
    }
}
