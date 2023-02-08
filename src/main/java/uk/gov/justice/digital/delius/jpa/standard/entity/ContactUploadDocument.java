package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
