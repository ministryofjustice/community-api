package uk.gov.justice.digital.delius.data.api.deliusapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class NewRelease {
    private Long id;
    private LocalDate actualReleaseDate;
    private String releaseType;
    private String institution;
}
