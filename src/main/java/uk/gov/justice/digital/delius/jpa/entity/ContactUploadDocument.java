package uk.gov.justice.digital.delius.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CONTACT_UPLOAD_DOCUMENT")
public class ContactUploadDocument {

    @Id
    @Column(name = "CONTACT_UPLOAD_DOCUMENT_ID")
    private Long contactUploadDocumentId;

    @Column(name = "CONTACT_ID")
    private Long contactId;

    @Column(name = "DOCUMENT_NAME")
    private String documentName;
}
