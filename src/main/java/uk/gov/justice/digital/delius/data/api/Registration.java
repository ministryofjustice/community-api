package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Registration {
    @ApiModelProperty(value = "Unique id of this registration", example = "2500064995")
    private Long registrationId;
    @ApiModelProperty(value = "Unique id of this offender", example = "2500343964")
    private Long offenderId;
    @ApiModelProperty(value = "Register this offender has been added to. For example RoSH")
    private KeyValue register;
    @ApiModelProperty(value = "Type of register. For example Low RoSH")
    private KeyValue type;
    @ApiModelProperty(value = "Literal visual colour this register represents", example = "Amber")
    private String riskColour;
    @JsonFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "Date added to register", example = "2021-01-30")
    private LocalDate startDate;
    @JsonFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "Date probation should review if the offender should still be on still register", example = "2021-01-30")
    private LocalDate nextReviewDate;
    @ApiModelProperty(value = "Number of months a review should take place", example = "6")
    private Long reviewPeriodMonths;
    @ApiModelProperty(value = "Additional notes")
    private String notes;
    @ApiModelProperty(value = "Probation team that added the offender to the register")
    private KeyValue registeringTeam;
    @ApiModelProperty(value = "Probation officer who added the offender to the register")
    private StaffHuman registeringOfficer;
    @ApiModelProperty(value = "Probation area that added the offender to the register")
    private KeyValue registeringProbationArea;
    @ApiModelProperty(value = "Level of register. Only used for certain registers for example Lifer - Supervised")
    private KeyValue registerLevel;
    @ApiModelProperty(value = "Category of register. Only used for certain registers for example Hate Crime category")
    private KeyValue registerCategory;
    @ApiModelProperty(value = "true if the register is serious enough to warn the probation officer of risk to themselves")
    private boolean warnUser;
    @ApiModelProperty(value = "true if active")
    private boolean active;
    @JsonFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "Date removed from register", example = "2021-01-30")
    private LocalDate endDate;
    @ApiModelProperty(value = "Probation team that removed the offender from the register")
    private KeyValue deregisteringTeam;
    @ApiModelProperty(value = "Probation officer who removed the offender from the register")
    private StaffHuman deregisteringOfficer;
    @ApiModelProperty(value = "Probation area that removed the offender from the register")
    private KeyValue deregisteringProbationArea;
    @ApiModelProperty(value = "Additional notes about the de-registration")
    private String deregisteringNotes;
}
