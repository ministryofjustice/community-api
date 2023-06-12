package uk.gov.justice.digital.delius.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Provision {
    private Long provisionId;

    private String notes;

    private LocalDate startDate;

    private LocalDate finishDate;

    private KeyValue provisionType;

    private KeyValue category;


}
