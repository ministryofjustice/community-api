package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OffenderDetail {
    @ApiModelProperty(required = true)
    private Long offenderId;
    @ApiModelProperty(example = "Mr")
    private String title;
    @ApiModelProperty(example = "John")
    private String firstName;
    private List<String> middleNames;
    @ApiModelProperty(example = "Smith")
    private String surname;
    @ApiModelProperty(example = "Davis")
    private String previousSurname;
    @ApiModelProperty(name = "Preferred name or commonly known as", example = "Bob")
    private String preferredName;
    @ApiModelProperty(example = "1982-10-24")
    private LocalDate dateOfBirth;
    @ApiModelProperty(example = "Male")
    private String gender;
    private IDs otherIds;
    private ContactDetails contactDetails;
    private OffenderProfile offenderProfile;
    private List<OffenderAlias> offenderAliases;
    private List<OffenderManager> offenderManagers;
    @ApiModelProperty(value = "When true this record has been deleted")
    private Boolean softDeleted;
    @ApiModelProperty(value = "deprecated, use activeProbationManagedSentence", example = "1")
    private String currentDisposal;
    @ApiModelProperty(example = "National Data")
    private String partitionArea;
    @ApiModelProperty(value = "When true this record can only be viewed by specific probation staff")
    private Boolean currentRestriction;
    @ApiModelProperty(value = "Message to show staff who have not been included to view this record")
    private String restrictionMessage;
    @ApiModelProperty(value = "When true this record can not be viewed by specific probation staff")
    private Boolean currentExclusion;
    @ApiModelProperty(value = "Message to show staff who have been excluded from viewing this record")
    private String exclusionMessage;
    @ApiModelProperty(value = "current tier", example = "D_2")
    private String currentTier;
    @ApiModelProperty(value = "identifies if this person is on an active sentence of interest to probation", notes = "this is an alias of currentDisposal but in Boolean form")
    public boolean isActiveProbationManagedSentence() {
        return "1".equals(currentDisposal);
    }
}
