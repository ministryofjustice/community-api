package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "SPG_NOTIFICATION")
public class SpgNotification {
    @Id
    @SequenceGenerator(name = "SPG_NOTIFICATION_ID_GENERATOR", sequenceName = "SPG_NOTIFICATION_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SPG_NOTIFICATION_ID_GENERATOR")
    @Column(name = "SPG_NOTIFICATION_ID")
    private Long spgNotificationId;
    @JoinColumn(name = "BUSINESS_INTERACTION_ID")
    @ManyToOne
    private BusinessInteraction businessInteraction;
    @Column(name = "OFFENDER_ID")
    private Long offenderId;
    @Column(name = "UNIQUE_ID")
    private Long uniqueId;
    @Column(name = "DATE_CREATED")
    private LocalDateTime dateCreated;
    @Column(name = "PROCESSED_FLAG")
    private Long processedFlag;
    @Column(name = "PROCESSED_DATETIME")
    private LocalDateTime processedDatetime;
    @Column(name = "MESSAGE_ID")
    private String messageId;
    @Column(name = "ERROR_FLAG")
    private Long errorFlag;
    @Column(name = "ERROR_MESSAGE")
    private String errorMessage;
    @Column(name = "CONTROL_REFERENCE")
    private Long controlReference;
    @ManyToOne
    @JoinColumn(name = "SENDER_IDENTITY_ID")
    private ProbationArea senderIdentity;
    @ManyToOne
    @JoinColumn(name = "RECEIVER_IDENTITY_ID")
    private ProbationArea receiverIdentity;
    @Column(name = "RECEIVER_CONTROL_REFERENCE")
    private Long receiverControlReference;
    @Column(name = "MESSAGE_DIRECTION")
    private String messageDirection;
    @Column(name = "PARENT_ENTITY_ID")
    private Long parentEntityId;
    @Column(name = "SPG_MESSAGE_CONTEXT_ID")
    private Long spgMessageContextId;
    @Column(name = "SPG_INTERCHANGE_STATUS_ID")
    private Long spgInterchangeStatusId;
    @Column(name = "ROW_VERSION")
    @Builder.Default
    private Long rowVersion = 1L;
    @Column(name = "REGENERATED_NOTIFICATION_ID")
    private Long regeneratedNotificationId;
    @Column(name = "MT_COMPONENT_ID")
    private Long mtComponentId;
    @Column(name = "MT_THREAD_ID")
    private Long mtThreadId;
    @Column(name = "DATE_SUBMITTED")
    private LocalDateTime dateSubmitted;
    @Column(name = "EXPORT_TO_FILE_FLAG")
    private Long exportToFileFlag;
    @Column(name = "DATA_SCRIPT_MESSAGE_ID")
    private Long dataScriptMessageId;

}
