package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConvictionDocuments {
    private String convictionId;
    private List<OffenderDocumentDetail> documents;
}
