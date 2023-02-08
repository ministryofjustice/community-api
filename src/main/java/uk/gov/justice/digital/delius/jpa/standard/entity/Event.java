package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Where;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "EVENT")
public class Event {

    @Id
    @SequenceGenerator(name = "EVENT_ID_GENERATOR", sequenceName = "EVENT_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EVENT_ID_GENERATOR")
    @Column(name = "EVENT_ID")
    private Long eventId;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Column(name = "IN_BREACH")
    private boolean inBreach;

    @Column(name = "FTC_COUNT", nullable = false)
    private Long failureToComplyCount;

    @Column(name = "BREACH_END")
    private LocalDate breachEnd;

    @Column(name = "NOTES")
    private String notes;

    @Column(name = "EVENT_NUMBER")
    private String eventNumber;

    @Column(name = "ACTIVE_FLAG")
    private boolean activeFlag;

    @Column(name = "CONVICTION_DATE")
    private LocalDate convictionDate;

    @Column(name = "REFERRAL_DATE")
    private LocalDate referralDate;

    @Column(name = "SOFT_DELETED")
    @Builder.Default
    private boolean softDeleted = false;

    @Column(name = "PARTITION_AREA_ID")
    private Long partitionAreaId;

    @Column(name = "ROW_VERSION")
    private Long rowVersion;

    @Column(name = "PENDING_TRANSFER")
    private Long pendingTransfer;

    @Column(name = "PSS_RQMNT_FLAG")
    private Long postSentenceSupervisionRequirementFlag;

    @OneToOne(mappedBy = "event", cascade = {CascadeType.ALL})
    @ToString.Exclude
    private MainOffence mainOffence;

    @OneToMany(mappedBy = "event", cascade = {CascadeType.ALL})
    @Exclude
    private List<AdditionalOffence> additionalOffences;

    @OneToOne(mappedBy = "event", cascade = {CascadeType.ALL})
    private Disposal disposal;

    @OneToOne(mappedBy = "event", cascade = {CascadeType.ALL})
    private OGRSAssessment OGRSAssessment;

    @OneToMany(mappedBy = "event", cascade = {CascadeType.ALL})
    @Exclude
    private List<CourtAppearance> courtAppearances;

    @OneToMany(mappedBy = "event", cascade = {CascadeType.ALL})
    @Exclude
    private List<OrderManager> orderManagers;

    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;

    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;

    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;

    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;

    @JoinColumn(name = "COURT_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    @Exclude
    private Court court;

    @JoinColumn(name = "EVENT_ID")
    @OneToMany(fetch = FetchType.LAZY)
    @Where(clause = "SOFT_DELETED != 1")
    @Exclude
    private List<AdditionalSentence> additionalSentences;

    public boolean isActive() {
        return isActiveFlag() && !isSoftDeleted();
    }
}
