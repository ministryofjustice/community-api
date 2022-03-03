package uk.gov.justice.digital.delius.data.api.deliusapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewRelease {
    private Long id;
    private LocalDate actualReleaseDate;

    public NewRelease(LocalDate actualReleaseDate, String releaseType, String institution) {
        this.actualReleaseDate = actualReleaseDate;
        this.releaseType = releaseType;
        this.institution = institution;
    }

    private String releaseType;
    private String institution;


}
