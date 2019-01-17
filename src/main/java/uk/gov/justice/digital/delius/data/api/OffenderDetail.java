package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class OffenderDetail {
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
    private ContactDetails contactDetails;
    private OffenderProfile offenderProfile;
    private List<OffenderAlias> offenderAliases;
    private List<OffenderManager> offenderManagers;
    private Boolean softDeleted;
    private String currentDisposal;
    private String partitionArea;
    private Boolean currentRestriction;
    private String restrictionMessage;
    private Boolean currentExclusion;
    private String exclusionMessage;
}
