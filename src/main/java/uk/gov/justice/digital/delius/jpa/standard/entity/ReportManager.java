package uk.gov.justice.digital.delius.jpa.standard.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
