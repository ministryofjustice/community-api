package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffenderDocuments {
    @JsonInclude
    private List<OffenderDocumentDetail> documents;
    @JsonInclude
    private List<ConvictionDocuments> convictions;
}
