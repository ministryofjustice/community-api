package uk.gov.justice.digital.delius.data.api.alfresco;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchResult {
    private String numberOfDocuments;
    private String maxResults;
    private String pageSize;
    private String startIndex;
    private List<DocumentMeta> documents;

    public boolean hasDocumentId(String docId) {
        return documents.stream().anyMatch(doc -> doc.getId().equals(docId));
    }
}
