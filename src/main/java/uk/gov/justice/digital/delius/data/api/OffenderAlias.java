package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class OffenderAlias {
    private LocalDate dateOfBirth;
    private String firstName;
    private List<String> middleNames;
    private String surname;
    private String gender;

}
