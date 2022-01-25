package uk.gov.justice.digital.delius.data.api;

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
public class TeamManagedOffender {

  @ApiModelProperty(value = "The probation area code", example = "N55", required = true)
  private String probationAreaCode;

  @ApiModelProperty(value = "Probation area description", example = "Yorkshire and Humberside NPS")
  private String probationAreaDescription;

  @ApiModelProperty(value = "PDU or cluster code", example = "N556AT", required = true)
  private String pduCode;

  @ApiModelProperty(value = "PDU or cluster description", example = "Hull NPS")
  private String pduDescription;

  @ApiModelProperty(value = "LDU code", example = "N556AT001", required = true)
  private String lduCode;

  @ApiModelProperty(value = "LDU description", example = "Hull North NPS ")
  private String lduDescription;

  @ApiModelProperty(value = "Unique internal team identifier", example = "1432", required = true)
  private Long teamId;

  @ApiModelProperty(value = "Team code", example = "N556AT001A", required = true)
  private String teamCode;

  @ApiModelProperty(value = "Team description", example = "Hull North NPS team A")
  private String teamDescription;

  @ApiModelProperty(value = "Case reference number (CRN) in nDelius", example = "X87655", required = true)
  private String crnNumber;

  @ApiModelProperty(value = "Prison reference number in Nomis", example = "A8778AA", required = true)
  private String nomsNumber;

  @ApiModelProperty(value = "Internal offender identifier in nDelius", example = "109987", required = true)
  private Long offenderId;

  @ApiModelProperty(value = "The surname of the managed offender", example = "Smith", required = true)
  private String offenderSurname;

  @ApiModelProperty(value = "The middle names of the managed offender", example = "Brian James")
  private String offenderMiddleNames;

  @ApiModelProperty(value = "The forename of the managed offender", example = "Thomas", required = true)
  private String offenderForename;

  @ApiModelProperty(value = "The date of birth for this managed offender", example = "21/02/1987")
  private LocalDate offenderDob;

  @ApiModelProperty(value = "The staff code of the offender manager in nDelius", example = "CRA20223")
  private String staffCode;

  @ApiModelProperty(value = "The staff identifier of the offender manager in nDelius", example = "1088777")
  private Long staffIdentifier;

  @ApiModelProperty(value = "The username of the offender manager in nDelius", example = "X766SNI")
  private String staffUsername;

  @ApiModelProperty(value = "The offender manager surname", example = "Cartwright")
  private String staffSurname;

  @ApiModelProperty(value = "The offender manager forename", example = "Ben")
  private String staffForename;

  @ApiModelProperty(
      value = "True if no staff member is allocated or when the unallocated staff member in this team is assigned",
      example = "true",
      required = true
  )
  private boolean allocated;
}
