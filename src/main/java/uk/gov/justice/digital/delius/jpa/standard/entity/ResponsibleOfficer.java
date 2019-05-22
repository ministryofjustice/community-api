package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "RESPONSIBLE_OFFICER")
public class ResponsibleOfficer {

    @Id
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
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;
}
