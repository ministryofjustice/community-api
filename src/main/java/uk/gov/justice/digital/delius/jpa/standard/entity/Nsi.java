package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "NSI")
public class Nsi {
    @Id
    @Column(name = "NSI_ID")
    private Long nsiId;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @JoinColumn(name = "EVENT_ID")
    @ManyToOne
    private Event event;

    @Column(name = "REFERRAL_DATE")
    private LocalDate referralDate;

    @Column(name = "ACTUAL_START_DATE")
    private LocalDate actualStartDate;

    @Column(name = "NSI_STATUS_DATE")
    private LocalDateTime nsiStatusDateTime;

    @Column(name = "EXPECTED_START_DATE")
    private LocalDate expectedStartDate;

    @JoinColumn(name = "NSI_TYPE_ID")
    @OneToOne
    private NsiType nsiType;

    @JoinColumn(name = "NSI_SUB_TYPE_ID")
    @OneToOne
    private StandardReference nsiSubType;

    @JoinColumn(name = "RQMNT_ID")
    @OneToOne
    private Requirement rqmnt;

    @JoinColumn(name = "NSI_OUTCOME_ID")
    @OneToOne
    private StandardReference nsiOutcome;

    @JoinColumn(name = "NSI_STATUS_ID")
    @OneToOne
    private NsiStatus nsiStatus;

    @Column(name = "LENGTH")
    private Long length;

    @OneToMany(mappedBy = "nsi")
    private List<NsiManager> nsiManagers;

    @Column(name = "NOTES")
    private String notes;

    @JoinColumn(name = "INTENDED_PROVIDER_ID")
    @ManyToOne
    private ProbationArea intendedProvider;

    @Column(name = "SOFT_DELETED")
    private Long softDeleted = 0L;

    @Column(name = "ACTIVE_FLAG")
    private Long activeFlag = 0L;

    public boolean isSoftDeleted() {
        return this.softDeleted != 0L;
    }
    public boolean isActive() { return this.activeFlag != 0L; }

}
