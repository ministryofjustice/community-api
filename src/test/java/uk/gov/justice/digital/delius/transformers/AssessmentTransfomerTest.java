package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.OffenderAssessments;
import uk.gov.justice.digital.delius.jpa.standard.entity.OASYSAssessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.OGRSAssessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class AssessmentTransfomerTest {

    @Test
    void noOasysAssessmentUsesOGRSAssessment() {
        assertThat(AssessmentTransformer.assessmentsOf(Offender.builder()
            .dynamicRsrScore(1.65)
            .build(), OGRSAssessment.builder().OGRS3Score2(2).build(), null))
            .isEqualTo(OffenderAssessments.builder().rsrScore(1.65).OGRSScore(2).build());
    }

    @Test
    void noOGRSAssessmentUsesOASYSAssessment() {
        assertThat(AssessmentTransformer.assessmentsOf(Offender.builder()
            .dynamicRsrScore(1D)
            .build(), null, OASYSAssessment.builder().OGRSScore2(44).build()))
            .isEqualTo(OffenderAssessments.builder().rsrScore(1D).OGRSScore(44).build());
    }

    @Test
    void noOASYSScoreUsesOGRSAssessment() {
        assertThat(AssessmentTransformer.assessmentsOf(Offender.builder()
            .dynamicRsrScore(1.65)
            .build(), OGRSAssessment.builder().OGRS3Score2(2).build(), OASYSAssessment.builder().build()))
            .isEqualTo(OffenderAssessments.builder().rsrScore(1.65).OGRSScore(2).build());
    }

    @Test
    void noOGRSScoreUsesOASYSAssessment() {
        assertThat(AssessmentTransformer.assessmentsOf(Offender.builder()
            .dynamicRsrScore(1.65)
            .build(), OGRSAssessment.builder().build(), OASYSAssessment.builder().OGRSScore2(4).build()))
            .isEqualTo(OffenderAssessments.builder().rsrScore(1.65).OGRSScore(4).build());
    }

    @Test
    void bothPresentBothHaveAssessmentOGRSIsMoreRecent() {
        assertThat(AssessmentTransformer.assessmentsOf(Offender.builder()
            .dynamicRsrScore(0.12)
            .build(), OGRSAssessment.builder().assessmentDate(LocalDate.of(2020,1,1)).OGRS3Score2(11).build(), OASYSAssessment.builder().assessmentDate(LocalDate.of(2018,1,1)).OGRSScore2(33).build()))
            .isEqualTo(OffenderAssessments.builder().rsrScore(0.12).OGRSScore(11).build());
    }

    @Test
    void bothPresentBothHaveAssessmentOASYSIsMoreRecent() {
        assertThat(AssessmentTransformer.assessmentsOf(Offender.builder()
            .dynamicRsrScore(0.12)
            .build(), OGRSAssessment.builder().assessmentDate(LocalDate.of(2018,1,1)).OGRS3Score2(11).build(), OASYSAssessment.builder().assessmentDate(LocalDate.of(2020,1,1)).OGRSScore2(33).build()))
            .isEqualTo(OffenderAssessments.builder().rsrScore(0.12).OGRSScore(33).build());
    }

    @Test
    void neitherPresentReturnsNull() {
        assertThat(AssessmentTransformer.assessmentsOf(Offender.builder()
            .dynamicRsrScore(0.12)
            .build(), null, null))
            .isEqualTo(OffenderAssessments.builder().rsrScore(0.12).build());
    }


}
