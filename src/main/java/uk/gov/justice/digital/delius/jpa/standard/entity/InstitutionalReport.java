package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "INSTITUTIONAL_REPORT")
public class InstitutionalReport {
    @Id
    @Column(name = "INSTITUTIONAL_REPORT_ID")
    private Long institutionalReportId;

    @JoinColumn(name = "CUSTODY_ID", referencedColumnName = "CUSTODY_ID")
    @OneToOne
    private Custody custody;

    @Column(name = "SOFT_DELETED")
    private Long softDeleted;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Column(name = "DATE_REQUESTED")
    private LocalDate dateRequested;

    @Column(name = "DATE_REQUIRED")
    private LocalDate dateRequired;

    @Column(name = "DATE_COMPLETED")
    private LocalDate dateCompleted;

    @ManyToOne
    @JoinColumn(name = "INSTITUTION_REPORT_TYPE_ID")
    private StandardReference institutionalReportType;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "INSTITUTION_ID"),
            @JoinColumn(name = "ESTABLISHMENT")})
    private RInstitution institution;

}
