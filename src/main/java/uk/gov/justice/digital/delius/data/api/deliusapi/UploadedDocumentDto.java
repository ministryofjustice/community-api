package uk.gov.justice.digital.delius.data.api.deliusapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadedDocumentDto {
    private Long id;
    private String remoteDocumentId;
    private String documentName;
    private String crn;
    private Long entityId;
    private String entityType;
    private String documentType;
    private String author;
    private String createdProvider;
    private String authorProvider;
    private String lockOwner;
    private Boolean locked;
    private Boolean reserved;
    private Boolean docRenamed;
    private String reservationOwner;
    private LocalDateTime dateLastModified;
    private String lastModifiedUser;
    private LocalDateTime creationDate;
}
