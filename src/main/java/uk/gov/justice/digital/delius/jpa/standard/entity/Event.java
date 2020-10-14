package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "EVENT")
@ToString(exclude = {"mainOffence", "additionalOffences", "courtAppearances", "orderManagers"})
public class Event {

    @Id
    @SequenceGenerator(name = "EVENT_ID_GENERATOR", sequenceName = "EVENT_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EVENT_ID_GENERATOR")
    @Column(name = "EVENT_ID")
    private Long eventId;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Column(name = "IN_BREACH")
    private Long inBreach;

    @Column(name = "NOTES")
    private String notes;

    @Column(name = "EVENT_NUMBER")
    private String eventNumber;

    @Column(name = "ACTIVE_FLAG")
    private Long activeFlag;

    @Column(name = "CONVICTION_DATE")
    private LocalDate convictionDate;

    @Column(name = "REFERRAL_DATE")
    private LocalDate referralDate;

    @Column(name = "SOFT_DELETED")
    @Builder.Default
    private Long softDeleted = 0L;

    @Column(name = "PARTITION_AREA_ID")
    private Long partitionAreaId;

    @Column(name = "ROW_VERSION")
    private Long rowVersion;

    @Column(name = "PENDING_TRANSFER")
    private Long pendingTransfer;

    @Column(name = "PSS_RQMNT_FLAG")
    private Long postSentenceSupervisionRequirementFlag;

    @OneToOne(mappedBy = "event", cascade = {CascadeType.ALL})
    private MainOffence mainOffence;

    @OneToMany(mappedBy = "event", cascade = {CascadeType.ALL})
    private List<AdditionalOffence> additionalOffences;

    @OneToOne(mappedBy = "event", cascade = {CascadeType.ALL})
    private Disposal disposal;

    @OneToMany(mappedBy = "event", cascade = {CascadeType.ALL})
    private List<CourtAppearance> courtAppearances;

    @OneToMany(mappedBy = "event", cascade = {CascadeType.ALL})
    private List<OrderManager> orderManagers;

    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;

    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;

    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;

    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;

    @Column(name = "CPS_ALFRESCO_DOCUMENT_ID")
    private String cpsAlfrescoDocumentId;

    @Column(name = "CPS_DOCUMENT_NAME")
    private String cpsDocumentName;

    @JoinColumn(name = "CPS_CREATED_BY_USER_ID", referencedColumnName = "USER_ID")
    @ManyToOne
    private User cpsCreatedByUser;

    @Column(name = "CPS_CREATED_DATETIME")
    private LocalDateTime cpsCreatedDatetime;

    @Column(name = "CPS_DATE")
    private LocalDate cpsDate;

    @Column(name = "CPS_SOFT_DELETED")
    private Long cpsSoftDeleted;

    @JoinColumn(name = "COURT_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Court court;

    public boolean hasCpsPack() {
        return !StringUtils.isEmpty(cpsAlfrescoDocumentId)
                && Optional.ofNullable(cpsSoftDeleted).orElse(0L).equals(0L);
    }

    public boolean isActive() {
        return getActiveFlag() == 1L && getSoftDeleted() != 1L;
    }
}
