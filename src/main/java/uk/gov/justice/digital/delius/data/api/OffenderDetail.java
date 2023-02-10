package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(required = true)
    private Long offenderId;
    @Schema(example = "Mr")
    private String title;
    @Schema(example = "John")
    private String firstName;
    private List<String> middleNames;
    @Schema(example = "Smith")
    private String surname;
    @Schema(example = "Davis")
    private String previousSurname;
    @Schema(name = "Preferred name or commonly known as", example = "Bob")
    private String preferredName;
    @Schema(example = "1982-10-24")
    private LocalDate dateOfBirth;
    @Schema(example = "Male")
    private String gender;
    private IDs otherIds;
    private ContactDetails contactDetails;
    private OffenderProfile offenderProfile;
    private List<OffenderAlias> offenderAliases;
    private List<OffenderManager> offenderManagers;
    @Schema(description = "When true this record has been deleted")
    private Boolean softDeleted;
    @Schema(description = "deprecated, use activeProbationManagedSentence", example = "1")
    private String currentDisposal;
    @Schema(example = "National Data")
    private String partitionArea;
    @Schema(description = "When true this record can only be viewed by specific probation staff")
    private Boolean currentRestriction;
    @Schema(description = "Message to show staff who have not been included to view this record")
    private String restrictionMessage;
    @Schema(description = "When true this record can not be viewed by specific probation staff")
    private Boolean currentExclusion;
    @Schema(description = "Message to show staff who have been excluded from viewing this record")
    private String exclusionMessage;
    @Schema(description = "current tier", example = "D_2")
    private String currentTier;
    @Schema(description = "identifies if this person is on an active sentence of interest to probation. this is an alias of currentDisposal but in Boolean form")
    public boolean isActiveProbationManagedSentence() {
        return "1".equals(currentDisposal);
    }
}
