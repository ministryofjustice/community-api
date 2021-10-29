package uk.gov.justice.digital.delius.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadedDocumentCreateResponse {
    private Long id;
    private String documentName;
    private String crn;
    private String author;
    private LocalDateTime dateLastModified;
    private String lastModifiedUser;
    private LocalDateTime creationDate;
}