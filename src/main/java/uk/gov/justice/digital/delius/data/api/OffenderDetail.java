package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Data
@Builder(toBuilder = true)
public class OffenderDetail {
    private Optional<String> title;
    private String firstName;
    private Optional<String> middleNames;
    private String surname;
    private List<String> previousSurnames;
    private LocalDate dateOfBirth;
    private String gender;
    private IDs ids;
    private ContactDetails contactDetails;
    private OffenderProfile offenderProfile;
}
