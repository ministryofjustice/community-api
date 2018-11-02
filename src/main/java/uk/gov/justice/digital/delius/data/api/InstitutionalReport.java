package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InstitutionalReport {
    private Long institutionalReportId;
    private Long offenderId;
    private Conviction conviction;
}
