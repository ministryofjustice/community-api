package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentMeta {
    private String id;
    private String documentName;
    private String entityType;
    private String docType;
    private String author;
    private OffsetDateTime lastModifiedAt;
    private OffsetDateTime createdAt;
    private ObjectNode userData;
}
