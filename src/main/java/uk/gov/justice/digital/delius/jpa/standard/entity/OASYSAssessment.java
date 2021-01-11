package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "OASYS_ASSESSMENT")
@ToString
public class OASYSAssessment {

    @Id
    @Column(name = "OASYS_ASSESSMENT_ID")
    private Long OASYSAssessmentId;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Column(name = "ASSESSMENT_DATE")
    private LocalDate assessmentDate;

    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDate lastUpdatedDate;

    @Column(name = "OGRS_SCORE_2")
    private Integer OGRSScore2;

    @Column(name = "SOFT_DELETED")
    private Integer softDeleted;


}
