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
    private String title;
    private String firstName;
    private List<String> middleNames;
    private String surname;
    private String previousSurname;
    private LocalDate dateOfBirth;
    private String gender;
    private IDs otherIds;
    private ContactDetailsSummary contactDetails;
    private OffenderProfile offenderProfile;
    private Boolean softDeleted;
    private String currentDisposal;
    private String partitionArea;
    private Boolean currentRestriction;
    private Boolean currentExclusion;
}
