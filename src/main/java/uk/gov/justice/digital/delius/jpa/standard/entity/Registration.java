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
@Table(name = "REGISTRATION")
public class Registration {
    @Id
    @Column(name = "REGISTRATION_ID")
    private Long registrationId;
    @Column(name = "OFFENDER_ID")
    private Long offenderId;
    @ManyToOne
    @JoinColumn(name = "REGISTER_TYPE_ID")
    private RegisterType registerType;
    @Column(name = "REGISTRATION_DATE")
    private LocalDate registrationDate;
    @Column(name = "NEXT_REVIEW_DATE")
    private LocalDate nextReviewDate;
    @Column(name = "REGISTRATION_NOTES")
    private String registrationNotes;
    @Column(name = "SOFT_DELETED")
    private Long softDeleted;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;
    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;
    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;
    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;
    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;
    @Column(name = "DEREGISTERED")
    private Long deregistered;
    @OneToOne(mappedBy = "registration")
    private Deregistration deregistration;
    @ManyToOne
    @JoinColumn(name = "REGISTERING_STAFF_ID")
    private Staff registeringStaff;
    @ManyToOne
    @JoinColumn(name = "REGISTERING_TEAM_ID")
    private Team registeringTeam;
    @OneToOne
    @JoinColumn(name = "REGISTER_LEVEL_ID")
    private StandardReference registerLevel;
    @OneToOne
    @JoinColumn(name = "REGISTER_CATEGORY_ID")
    private StandardReference registerCategory;


}
