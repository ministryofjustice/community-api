package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IDs {
    private String CRN;
    private String PNCNumber;
    private String CRONumber;
    private String NINumber;
    private String NOMSNumber;
    private String immigrationNumber;
    private String mostRecentPrisonerNumber;
}
