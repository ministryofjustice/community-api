package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "DEREGISTRATION")
public class Deregistration {
    @Id
    @Column(name = "DEREGISTRATION_ID")
    private Long deregistrationId;
    @Column(name = "OFFENDER_ID")
    private Long offenderId;
    @Column(name = "DEREGISTRATION_DATE")
    private LocalDate deregistrationDate;
    @Column(name = "DEREGISTERING_NOTES")
    private String deregisteringNotes;
    @Column(name = "SOFT_DELETED")
    private Long softDeleted;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;
    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;
    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;
    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;
    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;
    @JoinColumn(name = "REGISTRATION_ID")
    @OneToOne
    private Registration registration;
    @ManyToOne
    @JoinColumn(name = "DEREGISTERING_STAFF_ID")
    private Staff deregisteringStaff;
    @ManyToOne
    @JoinColumn(name = "DEREGISTERING_TEAM_ID")
    private Team deregisteringTeam;
}
