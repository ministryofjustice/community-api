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
public class ContactSummary {
    @NotNull
    @Schema(name = "Contact id", required = true)
    private Long contactId;

    @Schema(name = "Contact start date & time", example = "2021-05-25T10:00:00+01:00")
    private OffsetDateTime contactStart;

    @Schema(name = "Contact end date & time", example = "2021-05-25T11:00:00+01:00")
    private OffsetDateTime contactEnd;

    @NotNull
    @Schema(name = "Contact type", required = true)
    private ContactType type;

    @Schema(name = "Office location")
    private OfficeLocation officeLocation;

    @Schema(name = "Notes")
    private String notes;

    @NotNull
    @Schema(name = "Provider", required = true)
    private KeyValue provider;

    @NotNull
    @Schema(name = "Team", required = true)
    private KeyValue team;

    @NotNull
    @Schema(name = "Staff", required = true)
    private StaffHuman staff;

    @Schema(name = "Sensitive contact flag", example = "true")
    private Boolean sensitive;

    @Schema(name = "Outcome")
    private AppointmentOutcome outcome;

    /**
     * The RAR activity flag.
     * @implNote this does NOT mean that this contact counts towards RAR.
     * @see ContactSummary::rarActivityMeta consumers looking contacts that count toward RAR should prefer this instead
     */
    @Schema(name = "RAR activity flag", example = "true")
    private Boolean rarActivity;

    @Schema(name = "Date time when contact was last updated", example = "2021-05-25T10:00:00+01:00")
    private OffsetDateTime lastUpdatedDateTime;

    @Schema(name = "Details of the person last updated the contact")
    private Human lastUpdatedByUser;

    @Schema(name = "RAR details, otherwise, this activity log entry is not counted in the RAR days calculation")
    private ContactRarActivity rarActivityDetail;

    @Schema(name = "Enforcement details of this contact")
    private Enforcement enforcement;

    @Schema(name = "Description")
    private String description;
}
