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
            .ogrsScore(getOGRSScore(OGRSAssessment, OASYSAssessment))
            .dateLastUpdates(offender.getLastUpdatedDateTime())
            .build();
    }

    private static Integer getOGRSScore(Optional<OGRSAssessment> OGRSAssessment, Optional<OASYSAssessment> OASYSAssessment) {

        Integer OASYSAssessmentScore = OASYSAssessment.map(o -> o.getOGRSScore2()).orElse(null);
        LocalDate OASYSAssessmentDate = OASYSAssessment.map(o -> o.getAssessmentDate()).orElse(null);;

        return OGRSAssessment.map(OGRS -> {
            final var OGRSAssessmentScore = OGRS.getOGRS3Score2();
            if (null == OASYSAssessmentScore) {
                return OGRSAssessmentScore;
            }

            if (null != OGRSAssessmentScore) {
                final var OGRSAssessmentDate = OGRS.getAssessmentDate();
                if (OGRSAssessmentDate.isAfter(OASYSAssessmentDate)) {
                    return OGRSAssessmentScore;
                }
            }
            return null;
        }).orElse(OASYSAssessmentScore);


    }

}
