package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.util.Optional;

@Data
@Builder
public class IDs {
    private String CRN;
    private Optional<String> PNCNumber;
    private Optional<String> CRONumber;
    private Optional<String> NINumber;
    private Optional<String> NOMSNumber;
    private Optional<String> immigrationNumber;
    private Optional<String> mostRecentPrisonerNumber;
}
