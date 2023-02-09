package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Registration {
    @Schema(description = "Unique id of this registration", example = "2500064995")
    private Long registrationId;
    @Schema(description = "Unique id of this offender", example = "2500343964")
    private Long offenderId;
    @Schema(description = "Register this offender has been added to. For example RoSH")
    private KeyValue register;
    @Schema(description = "Type of register. For example Low RoSH")
    private KeyValue type;
    @Schema(description = "Literal visual colour this register represents", example = "Amber")
    private String riskColour;
    @JsonFormat(pattern="yyyy-MM-dd")
    @Schema(description = "Date added to register", example = "2021-01-30")
    private LocalDate startDate;
    @JsonFormat(pattern="yyyy-MM-dd")
    @Schema(description = "Date probation should review if the offender should still be on still register", example = "2021-01-30")
    private LocalDate nextReviewDate;
    @Schema(description = "Number of months a review should take place", example = "6")
    private Long reviewPeriodMonths;
    @Schema(description = "Additional notes")
    private String notes;
    @Schema(description = "Probation team that added the offender to the register")
    private KeyValue registeringTeam;
    @Schema(description = "Probation officer who added the offender to the register")
    private StaffHuman registeringOfficer;
    @Schema(description = "Probation area that added the offender to the register")
    private KeyValue registeringProbationArea;
    @Schema(description = "Level of register. Only used for certain registers for example Lifer - Supervised")
    private KeyValue registerLevel;
    @Schema(description = "Category of register. Only used for certain registers for example Hate Crime category")
    private KeyValue registerCategory;
    @Schema(description = "true if the register is serious enough to warn the probation officer of risk to themselves")
    private boolean warnUser;
    @Schema(description = "true if active")
    private boolean active;
    @JsonFormat(pattern="yyyy-MM-dd")
    @Schema(description = "Latest Date removed from register", example = "2021-01-30")
    private LocalDate endDate;
    @Schema(description = "Latest Probation team that removed the offender from the register")
    private KeyValue deregisteringTeam;
    @Schema(description = "Latest Probation officer who removed the offender from the register")
    private StaffHuman deregisteringOfficer;
    @Schema(description = "Latest Probation area that removed the offender from the register")
    private KeyValue deregisteringProbationArea;
    @Schema(description = "Latest Additional notes about the de-registration")
    private String deregisteringNotes;
    @Schema(description = "Count of number times this was de-registered")
    private int numberOfPreviousDeregistrations;
    List<RegistrationReview> registrationReviews;
}
