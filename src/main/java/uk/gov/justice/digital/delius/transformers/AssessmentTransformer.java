package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.OffenderAssessments;
import uk.gov.justice.digital.delius.jpa.standard.entity.OASYSAssessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.OGRSAssessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;

import java.time.LocalDate;
import java.util.Optional;

public class AssessmentTransformer {

    public static OffenderAssessments assessmentsOf(Offender offender, Optional<OGRSAssessment> OGRSAssessment, Optional<OASYSAssessment> OASYSAssessment) {
        return OffenderAssessments
            .builder()
            .rsrScore(offender.getDynamicRsrScore())
            .OGRSScore(getOGRSScore(OGRSAssessment, OASYSAssessment))
            .build();
    }

    private static Integer getOGRSScore(Optional<OGRSAssessment> OGRSAssessment, Optional<OASYSAssessment> OASYSAssessment) {

        Integer OASYSAssessmentScore = null;
        LocalDate OASYSAssessmentDate = null;

        if (OASYSAssessment.isPresent()) {
            OASYSAssessmentScore = OASYSAssessment.get().getOGRSScore2();
            OASYSAssessmentDate = OASYSAssessment.get().getAssessmentDate();
        }

        Integer finalOASYSAssessmentScore = OASYSAssessmentScore;
        LocalDate finalOASYSAssessmentDate = OASYSAssessmentDate;

        return OGRSAssessment.map(OGRS -> {
            final var OGRSAssessmentScore = OGRS.getOGRS3Score2();
            final var OGRSAssessmentDate = OGRS.getAssessmentDate();
            if (null == finalOASYSAssessmentScore) {
                return OGRSAssessmentScore;
            }

            if (null != OGRSAssessmentScore) {
                if (OGRSAssessmentDate.isAfter(finalOASYSAssessmentDate)) {
                    return OGRSAssessmentScore;
                }
            }
            return null;
        }).orElse(OASYSAssessmentScore);


    }

}
