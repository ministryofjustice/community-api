package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class DocumentMeta {
    private String id;
    private String documentName;
    private String entityType;
    private String docType;
    private OffsetDateTime lastModifiedAt;
    private OffsetDateTime createdAt;

}
