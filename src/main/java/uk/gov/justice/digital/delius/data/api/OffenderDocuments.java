package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OffenderDocuments {
    private List<OffenderDocumentDetail> documents;
    private List<ConvictionDocuments> convictions;
}
