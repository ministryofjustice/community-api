package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.OffenderAssessments;
import uk.gov.justice.digital.delius.jpa.standard.entity.OASYSAssessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.OGRSAssessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;

import java.time.LocalDate;
import java.util.Optional;

public class AssessmentTransformer {

    public static OffenderAssessments assessmentsOf(Offender offender, Optional<OGRSAssessment> ogrsAssessment, Optional<OASYSAssessment> OASYSAssessment) {
        return OffenderAssessments
            .builder()
            .rsrScore(offender.getDynamicRsrScore())
            .ogrsScore(getOGRSScore(ogrsAssessment, OASYSAssessment))
            .orgsLastUpdate(ogrsAssessment.map(OGRSAssessment::getLastUpdatedDate).orElse(null))
            .build();
    }

    private static Integer getOGRSScore(Optional<OGRSAssessment> oGRSAssessment, Optional<OASYSAssessment> oASYSAssessment) {

        Integer oASYSAssessmentScore = oASYSAssessment.map(OASYSAssessment::getOGRSScore2).orElse(null);
        LocalDate oASYSAssessmentDate = oASYSAssessment.map(OASYSAssessment::getAssessmentDate).orElse(null);


        return oGRSAssessment.map(OGRS -> {
            var ogrsassessmentscore = OGRS.getOGRS3Score2();

            if (null == oASYSAssessmentScore) {
                return ogrsassessmentscore;
            }
            if (null != ogrsassessmentscore) {
                if (OGRS.getAssessmentDate().isAfter(oASYSAssessmentDate)) {
                    return ogrsassessmentscore;
                }
            }
            return null;
        }).orElse(oASYSAssessmentScore);


    }

}
