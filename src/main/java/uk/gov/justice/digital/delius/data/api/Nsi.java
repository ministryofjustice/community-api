package uk.gov.justice.digital.delius.data.api;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Nsi {
    private Long nsiId;
    private KeyValue nsiType;
    private KeyValue nsiSubType;
    private Requirement requirement;
    private KeyValue nsiStatus;
    private LocalDate actualStartDate;
    private LocalDate expectedStartDate;
    private LocalDate referralDate;

}
