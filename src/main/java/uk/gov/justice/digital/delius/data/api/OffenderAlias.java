package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Data
@Builder
public class OffenderAlias {

    private Optional<LocalDate> dateOfBirth;
    private Optional<String> firstName;
    private List<String> middleNames;
    private Optional<String> surname;
    private Optional<String> gender;

}
