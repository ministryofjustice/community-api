package uk.gov.justice.digital.delius.jpa.standard.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "REPORT_MANAGER")
public class ReportManager {
    @Id
    @Column(name = "REPORT_MANAGER_ID")
    private Long reportManagerId;

    @Column(name = "ACTIVE_FLAG", nullable = false)
    private boolean active;

    @Column(name = "ALLOCATION_DATE", nullable = false)
    private LocalDateTime allocationDate;

    @JoinColumn(name = "COURT_REPORT_ID")
    @ManyToOne
    private CourtReport courtReport;

    @Column(name = "CREATED_BY_USER_ID", nullable = false)
    private Long createdByUserId;

    @Column(name = "CREATED_DATETIME", nullable = false)
    private LocalDateTime createdDatetime;

    @Column(name = "END_DATE")
    private LocalDateTime endDate;

    @Column(name = "LAST_UPDATED_USER_ID", nullable = false)
    private Long lastUpdatedUserId;

    @Column(name = "LAST_UPDATED_DATETIME", nullable = false)
    private LocalDateTime lastUpdatedDatetime;

    @Column(name = "ROW_VERSION", nullable = false)
    private Long rowVersion;

    @Column(name = "SOFT_DELETED", nullable = false)
    private boolean softDeleted;

    @JoinColumn(name = "STAFF_ID")
    @OneToOne
    private Staff staff;

}
