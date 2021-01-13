package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.OffenderAssessments;
import uk.gov.justice.digital.delius.jpa.standard.entity.OASYSAssessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.OGRSAssessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class AssessmentTransfomerTest {

    @Test
    void noOASYSAssessmentUsesOGRSAssessment() {
        assertThat(AssessmentTransformer.assessmentsOf(Offender.builder()
            .dynamicRsrScore(1.65)
            .build(), Optional.of(OGRSAssessment.builder().OGRS3Score2(2).build()), Optional.empty()))
            .isEqualTo(OffenderAssessments.builder().rsrScore(1.65).ogrsScore(2).build());
    }

    @Test
    void noOGRSAssessmentUsesOASYSAssessment() {
        assertThat(AssessmentTransformer.assessmentsOf(Offender.builder()
            .dynamicRsrScore(1D)
            .build(), Optional.empty(), Optional.of(OASYSAssessment.builder().OGRSScore2(44).build())))
            .isEqualTo(OffenderAssessments.builder().rsrScore(1D).ogrsScore(44).build());
    }

    @Test
    void noOASYSScoreUsesOGRSAssessment() {
        assertThat(AssessmentTransformer.assessmentsOf(Offender.builder()
            .dynamicRsrScore(1.65)
            .build(), Optional.of(OGRSAssessment.builder().OGRS3Score2(2).build()), Optional.of(OASYSAssessment.builder().build())))
            .isEqualTo(OffenderAssessments.builder().rsrScore(1.65).ogrsScore(2).build());
    }

    @Test
    void noOGRSScoreUsesOASYSAssessment() {
        assertThat(AssessmentTransformer.assessmentsOf(Offender.builder()
            .dynamicRsrScore(1.65)
            .build(), Optional.of(OGRSAssessment.builder().build()), Optional.of(OASYSAssessment.builder().OGRSScore2(4).build())))
            .isEqualTo(OffenderAssessments.builder().rsrScore(1.65).ogrsScore(4).build());
    }

    @Test
    void bothPresentBothHaveAssessmentOGRSIsMoreRecent() {
        assertThat(AssessmentTransformer.assessmentsOf(Offender.builder()
            .dynamicRsrScore(0.12)
            .build(), Optional.of(OGRSAssessment.builder().assessmentDate(LocalDate.of(2020,1,1)).OGRS3Score2(11).build()),
            Optional.of(OASYSAssessment.builder().assessmentDate(LocalDate.of(2018,1,1)).OGRSScore2(33).build())))
            .isEqualTo(OffenderAssessments.builder().rsrScore(0.12).ogrsScore(11).build());
    }

    @Test
    void bothPresentBothHaveAssessmentOASYSIsMoreRecent() {
        assertThat(AssessmentTransformer.assessmentsOf(Offender.builder()
            .dynamicRsrScore(0.12)
            .build(), Optional.of(OGRSAssessment.builder().assessmentDate(LocalDate.of(2018,1,1)).OGRS3Score2(11).build()),
            Optional.of(OASYSAssessment.builder().assessmentDate(LocalDate.of(2020,1,1)).OGRSScore2(33).build())))
            .isEqualTo(OffenderAssessments.builder().rsrScore(0.12).ogrsScore(33).build());
    }

    @Test
    void neitherPresentReturnsNull() {
        assertThat(AssessmentTransformer.assessmentsOf(Offender.builder()
            .dynamicRsrScore(0.12)
            .build(), Optional.empty(), Optional.empty()))
            .isEqualTo(OffenderAssessments.builder().rsrScore(0.12).build());
    }


}
