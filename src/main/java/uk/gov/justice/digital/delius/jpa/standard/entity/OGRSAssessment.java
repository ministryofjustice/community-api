package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "OGRS_ASSESSMENT")
@ToString
public class OGRSAssessment {

    @Id
    @Column(name = "OGRS_ASSESSMENT_ID")
    private Long OGRSAssessmentId;

    @Column(name = "ASSESSMENT_DATE")
    private LocalDate assessmentDate;

    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDate lastUpdatedDate;

    @Column(name = "OGRS3_SCORE_2")
    private Integer OGRS3Score2;

    @Column(name = "SOFT_DELETED")
    private Integer softDeleted;

    @JoinColumn(name = "EVENT_ID", referencedColumnName = "EVENT_ID")
    @OneToOne
    private Event event;

}
