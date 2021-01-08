package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.OffenderAssessments;
import uk.gov.justice.digital.delius.jpa.standard.entity.OGRSAssessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;

public class AssessmentTransformer {

    public static OffenderAssessments assessmentsOf(Offender offender, OGRSAssessment OGRSAssessment) {
        return OffenderAssessments.builder().rsrScore(offender.getDynamicRsrScore()).OGRSScore(OGRSAssessment.getOGRS3Score2()).build();
    }

}
