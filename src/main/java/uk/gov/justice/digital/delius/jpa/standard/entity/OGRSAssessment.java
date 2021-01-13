package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
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
