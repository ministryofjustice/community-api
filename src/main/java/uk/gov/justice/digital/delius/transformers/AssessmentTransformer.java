package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.OffenderAssessments;
import uk.gov.justice.digital.delius.jpa.standard.entity.OASYSAssessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.OGRSAssessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;

import java.time.LocalDate;
import java.util.Optional;

public class AssessmentTransformer {

    public static OffenderAssessments assessmentsOf(Offender offender, Optional<OGRSAssessment> ogrsAssessment, Optional<OASYSAssessment> oasysAssessment) {
        return OffenderAssessments
            .builder()
            .rsrScore(offender.getDynamicRsrScore())
            .ogrsScore(getOGRSScore(ogrsAssessment, oasysAssessment))
            .orgsLastUpdate(ogrsAssessment.map(OGRSAssessment::getLastUpdatedDate).orElse(null))
            .build();
    }

    private static Integer getOGRSScore(Optional<OGRSAssessment> ogrsAssessment, Optional<OASYSAssessment> oasysAssessment) {

        Integer oasysAssessmentScore = oasysAssessment.map(OASYSAssessment::getOGRSScore2).orElse(null);
        LocalDate oasysAssessmentDate = oasysAssessment.map(OASYSAssessment::getAssessmentDate).orElse(null);


        return ogrsAssessment.map(OGRS -> {
            var ogrsAssessmentscore = OGRS.getOGRS3Score2();

            if (null == oasysAssessmentScore) {
                return ogrsAssessmentscore;
            }
            if (null != ogrsAssessmentscore) {
                if (OGRS.getAssessmentDate().isAfter(oasysAssessmentDate)) {
                    return ogrsAssessmentscore;
                }
            }
            return null;
        }).orElse(oasysAssessmentScore);


    }

}
