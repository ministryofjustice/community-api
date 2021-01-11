package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.OffenderAssessments;
import uk.gov.justice.digital.delius.jpa.standard.entity.OASYSAssessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.OGRSAssessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;

public class AssessmentTransformer {

    public static OffenderAssessments assessmentsOf(Offender offender, OGRSAssessment OGRSAssessment, OASYSAssessment OASYSAssessment) {
        return OffenderAssessments
            .builder()
            .rsrScore(offender.getDynamicRsrScore())
            .OGRSScore(getOGRSScore(OGRSAssessment, OASYSAssessment))
            .build();
    }

    private static Integer getOGRSScore(OGRSAssessment OGRSAssessment, OASYSAssessment OASYSAssessment) {
        if (null != OGRSAssessment) {
            if(null != OGRSAssessment.getOGRS3Score2()){
                return OGRSAssessment.getOGRS3Score2();
            }
        }
        if (null != OASYSAssessment) {
            return OASYSAssessment.getOGRSScore2();
        }
        return null;
    }

}
