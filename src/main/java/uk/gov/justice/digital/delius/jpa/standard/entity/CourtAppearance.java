package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString.Exclude;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Data
@Builder(toBuilder = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "COURT_APPEARANCE")
@NamedEntityGraph(name = "ContactAppearance.minimal",
    attributeNodes = {
        @NamedAttributeNode("offenderId"),
        @NamedAttributeNode("courtAppearanceId"),
        @NamedAttributeNode("appearanceDate"),
        @NamedAttributeNode(value = "court", subgraph = "Court.minimal"),
        @NamedAttributeNode(value = "appearanceType", subgraph = "AppearanceType.minimal")
    },
    subgraphs = {
        @NamedSubgraph(name = "Court.minimal", attributeNodes = {
            @NamedAttributeNode("code"),
            @NamedAttributeNode("courtName")
        }),
        @NamedSubgraph(name = "AppearanceType.minimal", attributeNodes = {
            @NamedAttributeNode("codeValue"),
            @NamedAttributeNode("codeDescription")
        })
    }

)
public class CourtAppearance {
    enum CourtAppearanceType {
        SENTENCING("S");

        private final String code;

        CourtAppearanceType(String code) {
            this.code = code;
        }
        public String getCode() {
            return code;
        }
    }

    @Id
    @SequenceGenerator(name = "COURT_APPEARANCE_ID_GENERATOR", sequenceName = "COURT_APPEARANCE_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COURT_APPEARANCE_ID_GENERATOR")
    @Column(name = "COURT_APPEARANCE_ID")
    private Long courtAppearanceId;

    @Column(name = "APPEARANCE_DATE")
    private LocalDateTime appearanceDate;

    @Column(name = "CROWN_COURT_CALENDAR_NUMBER")
    private String crownCourtCalendarNumber;

    @Column(name = "BAIL_CONDITIONS")
    private String bailConditions;

    @Column(name = "COURT_NOTES")
    private String courtNotes;

    @JoinColumn(name = "EVENT_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    @Exclude
    private Event event;

    @Column(name = "TEAM_ID")
    private Long teamId;

    @Column(name = "STAFF_ID")
    private Long staffId;

    @Column(name = "SOFT_DELETED")
    private Long softDeleted;

    @Column(name = "PARTITION_AREA_ID")
    private Long partitionAreaId;

    @JoinColumn(name = "COURT_ID")
    @ManyToOne
    private Court court;

    @Column(name = "ROW_VERSION")
    private Long rowVersion;

    @Column(name = "APPEARANCE_TYPE_ID")
    private Long appearanceTypeId;

    @Column(name = "PLEA_ID")
    private Long pleaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OUTCOME_ID")
    @Exclude
    private StandardReference outcome;

    @Column(name = "REMAND_STATUS_ID")
    private Long remandStatusId;

    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;

    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;

    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;

    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;

    @Column(name = "TRAINING_SESSION_ID")
    private Long trainingSessionId;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @OneToMany(mappedBy = "courtAppearance", cascade = {CascadeType.ALL})
    @Exclude
    private List<CourtReport> courtReports;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APPEARANCE_TYPE_ID", insertable = false, updatable = false)
    @Exclude
    private StandardReference appearanceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_ID", insertable = false, updatable = false)
    @Exclude
    private Offender offender;

    public boolean isSentencing() {
        return Optional.ofNullable(getAppearanceType())
            .map(x -> x.getCodeValue().equals(CourtAppearanceType.SENTENCING.getCode()))
            .orElse(false);
    }
}
