package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "ORDER_MANAGER")
public class OrderManager {

    @Column(name = "ORDER_MANAGER_ID")
    @SequenceGenerator(name = "ORDER_MANAGER_ID_GENERATOR", sequenceName = "ORDER_MANAGER_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ORDER_MANAGER_ID_GENERATOR")
    @Id
    private Long orderManagerId;

    @Column(name = "ALLOCATION_DATE")
    private LocalDateTime allocationDate;


    @JoinColumn(name = "ALLOCATION_TEAM_ID")
    @OneToOne
    private Team team;

    @JoinColumn(name = "ALLOCATION_STAFF_ID")
    @OneToOne
    private Staff staff;

    @Column(name = "SOFT_DELETED")
    private Long softDeleted;

    @Column(name = "END_DATE")
    private LocalDateTime endDate;

    @Column(name = "PARTITION_AREA_ID")
    private Long partitionAreaId;

    @Column(name = "ROW_VERSION")
    private Long rowVersion;

    @JoinColumn(name = "EVENT_ID")
    @ManyToOne
    @ToString.Exclude
    private Event event;

    @JoinColumn(name = "ALLOCATION_REASON_ID")
    @OneToOne
    private StandardReference allocationReason;


    @JoinColumn(name = "PROVIDER_EMPLOYEE_ID")
    @OneToOne
    private ProviderEmployee providerEmployee;

    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;

    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;

    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;

    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;

    @JoinColumn(name = "PROVIDER_TEAM_ID")
    @OneToOne
    private ProviderTeam providerTeam;

    @JoinColumn(name = "TRANSFER_REASON_ID")
    @ManyToOne
    private TransferReason transferReason;

    @JoinColumns({
            @JoinColumn(name = "STAFF_EMPLOYEE_ID", referencedColumnName = "STAFF_EMPLOYEE_ID", insertable = false, updatable = false),
            @JoinColumn(name = "TRUST_PROVIDER_FLAG", referencedColumnName = "TRUST_PROVIDER_FLAG", insertable = false, updatable = false)
    })
    @OneToOne
    private Officer officer;

    @JoinColumns({
            @JoinColumn(name = "TRUST_PROVIDER_TEAM_ID", referencedColumnName = "TRUST_PROVIDER_TEAM_ID", insertable = false, updatable = false),
            @JoinColumn(name = "TRUST_PROVIDER_FLAG", referencedColumnName = "TRUST_PROVIDER_FLAG", insertable = false, updatable = false)
    })
    @OneToOne
    private AllTeam trustProviderTeam;

    @JoinColumn(name = "PROBATION_AREA_ID")
    @OneToOne
    private ProbationArea probationArea;

    @Column(name = "ACTIVE_FLAG")
    private Long activeFlag;

    @Column(name = "ORDER_TRANSFER_ID")
    private Long orderTransferId;

    public boolean isActive() {
        return Optional.ofNullable(activeFlag).orElse(0L) == 1L && endDate == null;
    }


}
