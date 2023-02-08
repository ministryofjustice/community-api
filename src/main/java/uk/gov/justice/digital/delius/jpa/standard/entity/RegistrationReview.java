package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "REGISTRATION_REVIEW")
public class RegistrationReview {
    @Id
    @Column(name = "REGISTRATION_REVIEW_ID")
    private Long registrationReviewId;

    @Column(name = "REGISTRATION_ID")
    private Long registrationId;

    @Column(name = "REVIEW_DATE")
    private LocalDate reviewDate;

    @Column(name = "REVIEW_DATE_DUE")
    private LocalDate reviewDateDue;

    @Column(name = "NOTES")
    private String notes;

    @ManyToOne
    @JoinColumn(name = "REVIEWING_TEAM_ID")
    private Team reviewingTeam;

    @ManyToOne
    @JoinColumn(name = "REVIEWING_STAFF_ID")
    private Staff reviewingStaff;

    @Column(name = "COMPLETED")
    private boolean completed;

    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;

    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;

    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;

    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime updatedDatetime;

    @Column(name = "CONTACT_ID")
    private Long contactId;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;
}
