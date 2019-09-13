package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TABLE_NAME", discriminatorType = DiscriminatorType.STRING)
@Table(name = "DOCUMENT")
public class Document {

    @Id
    @SequenceGenerator(name = "DOCUMENT_DOCUMENTID_GENERATOR", sequenceName = "DOCUMENT_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DOCUMENT_DOCUMENTID_GENERATOR")
    @Column(name = "DOCUMENT_ID")
    private long documentId;

    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDate;

    @Column(name = "LAST_SAVED")
    private LocalDateTime lastSaved;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Column(name = "PARTITION_AREA_ID")
    @Builder.Default
    private Long partitionAreaId = 0L;

    @Column(name = "PRIMARY_KEY_ID")
    private Long primaryKeyId;

    @Version
    @Column(name = "ROW_VERSION")
    private Long rowVersion;

    @Column(name = "SOFT_DELETED")
    private boolean softDeleted;

    @JoinColumn(name = "CREATED_BY_USER_ID", referencedColumnName = "USER_ID")
    @ManyToOne
    private User createdByUser;

    @JoinColumn(name = "LAST_UPDATED_USER_ID", referencedColumnName = "USER_ID")
    @ManyToOne
    private User lastUpdatedByUser;

    @Column(name = "STATUS")
    @Builder.Default
    private String status = "N";

    @Column(name = "WORK_IN_PROGRESS")
    @Builder.Default
    private String workInProgress = "N";

    @Column(name = "TEMPLATE_NAME")
    private String templateName;

    @Column(name = "DOCUMENT_NAME")
    private String documentName;

    @Column(name = "ALFRESCO_DOCUMENT_ID")
    private String alfrescoId;

    @Column(name = "CREATED_PROVIDER_ID")
    private Long createdByProbationAreaId;

    @Column(name = "LAST_UPD_AUTHOR_PROVIDER_ID")
    private Long lastUpdatedByProbationAreaId;

}