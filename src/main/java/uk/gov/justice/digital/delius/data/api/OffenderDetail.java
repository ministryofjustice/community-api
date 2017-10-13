package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class OffenderDetail {
    private String title;
    private String firstName;
    private String middleNames;
    private String surname;
    private List<String> previousSurnames;
    private LocalDate dateOfBirth;
    private String gender;
    private IDs ids;
    private ContactDetails contactDetails;
    private OffenderProfile offenderProfile;
}
