package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.OffenderAssessments;
import uk.gov.justice.digital.delius.jpa.standard.entity.OASYSAssessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.OGRSAssessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import static org.assertj.core.api.Assertions.assertThat;

public class AssessmentTransfomerTest {

    @Test
    void noOasysScoreUsesOGRSAssessment() {
        assertThat(AssessmentTransformer.assessmentsOf(Offender.builder()
            .dynamicRsrScore(1)
            .build(), OGRSAssessment.builder().OGRS3Score2(2).build(), null))
            .isEqualTo(OffenderAssessments.builder().rsrScore(1).OGRSScore(2).build());
    }

    @Test
    void noOGRSScoreUsesOasysAssessment() {
        assertThat(AssessmentTransformer.assessmentsOf(Offender.builder()
            .dynamicRsrScore(1)
            .build(), null, OASYSAssessment.builder().OGRSScore2(44).build()))
            .isEqualTo(OffenderAssessments.builder().rsrScore(1).OGRSScore(44).build());
    }

    @Test
    void neitherPresentReturnsNull() {
        assertThat(AssessmentTransformer.assessmentsOf(Offender.builder()
            .dynamicRsrScore(1)
            .build(), null, null))
            .isEqualTo(OffenderAssessments.builder().rsrScore(1).build());
    }


}
