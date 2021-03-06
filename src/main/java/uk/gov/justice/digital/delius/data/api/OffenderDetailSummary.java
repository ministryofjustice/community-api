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
public class OffenderDetailSummary {
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
    @ApiModelProperty(example = "1982-10-24")
    private LocalDate dateOfBirth;
    @ApiModelProperty(example = "Male")
    private String gender;
    private IDs otherIds;
    private ContactDetailsSummary contactDetails;
    private OffenderProfile offenderProfile;
    @ApiModelProperty(value = "When true this record has been deleted")
    private Boolean softDeleted;
    @ApiModelProperty(value = "deprecated, use activeProbationManagedSentence", example = "1")
    private String currentDisposal;
    @ApiModelProperty(example = "National Data")
    private String partitionArea;
    @ApiModelProperty(value = "When true this record can only be viewed by specific probation staff")
    private Boolean currentRestriction;
    @ApiModelProperty(value = "When true this record can not be viewed by specific probation staff")
    private Boolean currentExclusion;
    @ApiModelProperty(value = "identifies if this person is on an active sentence of interest to probation", notes = "this is an alias of currentDisposal but in Boolean form")
    public boolean isActiveProbationManagedSentence() {
        return "1".equals(currentDisposal);
    }
}
