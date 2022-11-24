package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "RESPONSIBLE_OFFICER")
public class ResponsibleOfficer implements Serializable {

    @Id
    @SequenceGenerator(name = "RESPONSIBLE_OFFICER_ID_GENERATOR", sequenceName = "RESPONSIBLE_OFFICER_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RESPONSIBLE_OFFICER_ID_GENERATOR")
    @Column(name = "RESPONSIBLE_OFFICER_ID")
    private Long responsibleOfficerId;

    /*
     Currently - the responsible officer can be either the OM or the POM.
     Until the POM is populated with realistic data by the OMiC programme the RESPONSIBLE_OFFICER will not contain accurate data for Prison staff.
     JPA links to the OFFENDER entity should be via the RESPONSIBLE_OFFICER table but are currently via OFFENDER_MANAGER and PRISON_OFFENDER_MANAGER entiries.
     When real POMs are present it will make sense to remove existing links and add a single link via RESPONSIBLE_OFFICER to the OFFENDERS @manyToOne.
     */

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Column(name = "OFFENDER_MANAGER_ID")
    private Long offenderManagerId;

    @Column(name = "PRISON_OFFENDER_MANAGER_ID")
    private Long prisonOffenderManagerId;

    @Column(name = "START_DATE")
    @Builder.Default
    private LocalDateTime startDateTime = LocalDateTime.now();

    @Column(name = "END_DATE")
    private LocalDateTime endDateTime;

    @Column(name = "ROW_VERSION")
    @Builder.Default
    private Long rowVersion = 1L;

    @Column(name = "CREATED_BY_USER_ID")
    @CreatedBy
    private Long createdByUserId;

    @Column(name = "CREATED_DATETIME")
    @CreatedDate
    private LocalDateTime createdDatetime;

    @Column(name = "LAST_UPDATED_USER_ID")
    @LastModifiedBy
    private Long lastUpdatedUserId;

    @Column(name = "LAST_UPDATED_DATETIME")
    @LastModifiedDate
    private LocalDateTime lastUpdatedDatetime;

    protected boolean isActive() {
        return endDateTime == null;
    }

    public void makeInactive() {
        this.endDateTime = LocalDateTime.now();
    }
}
