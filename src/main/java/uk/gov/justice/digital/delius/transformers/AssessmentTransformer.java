package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.OffenderAssessments;
import uk.gov.justice.digital.delius.jpa.standard.entity.OASYSAssessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.OGRSAssessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;

import java.time.LocalDate;

public class AssessmentTransformer {

    public static OffenderAssessments assessmentsOf(Offender offender, OGRSAssessment OGRSAssessment, OASYSAssessment OASYSAssessment) {
        return OffenderAssessments
            .builder()
            .rsrScore(offender.getDynamicRsrScore())
            .OGRSScore(getOGRSScore(OGRSAssessment, OASYSAssessment))
            .build();
    }

    private static Integer getOGRSScore(OGRSAssessment OGRSAssessment, OASYSAssessment OASYSAssessment) {
        Integer OGRSAssessmentScore = null;
        Integer OASYSAssessmentScore = null;
        LocalDate OGRSAssessmentDate = null;
        LocalDate OASYSAssessmentDate = null;

        if (null != OGRSAssessment) {
            OGRSAssessmentScore = OGRSAssessment.getOGRS3Score2();
            OGRSAssessmentDate = OGRSAssessment.getAssessmentDate();
        }
        if (null != OASYSAssessment) {
            OASYSAssessmentScore = OASYSAssessment.getOGRSScore2();
            OASYSAssessmentDate = OASYSAssessment.getAssessmentDate();
        }
        if(null == OASYSAssessmentScore) {
            return OGRSAssessmentScore;
        }

        if (null != OGRSAssessmentScore) {
            if(OGRSAssessmentDate != null) {
                if (OGRSAssessmentDate.isAfter(OASYSAssessmentDate)) {
                    return OGRSAssessmentScore;
                }
            }
        }
        if (null != OASYSAssessmentScore) {
            return OASYSAssessmentScore;
        }

        return null;
    }

}
