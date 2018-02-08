package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import lombok.Data;
import uk.gov.justice.digital.delius.data.api.views.Views;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Data
@Builder(toBuilder = true)
public class OffenderDetail {
    private Long offenderId;
    private Optional<String> title;
    private String firstName;
    private List<String> middleNames;
    private String surname;
    private Optional<String> previousSurname;
    private LocalDate dateOfBirth;
    private String gender;
    private IDs otherIds;
    private ContactDetails contactDetails;
    private OffenderProfile offenderProfile;
    @JsonView(Views.FullFat.class)
    private Optional<List<OffenderAlias>> offenderAliases;
    private Boolean softDeleted;
    private Optional<String> currentDisposal;
    private Optional<String> partitionArea;
    private Boolean currentRestriction;
    private Boolean currentExclusion;
}
