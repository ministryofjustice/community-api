package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.OffenderAssessments;
import uk.gov.justice.digital.delius.jpa.standard.entity.OASYSAssessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.OGRSAssessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

public class AssessmentTransformer {

    public static OffenderAssessments assessmentsOf(Offender offender, Optional<OGRSAssessment> ogrsAssessment, Optional<OASYSAssessment> oasysAssessment) {
        return OffenderAssessments
            .builder()
            .rsrScore(offender.getDynamicRsrScore())
            .ogrsScore(getOGRSScore(ogrsAssessment, oasysAssessment))
            .ogrsLastUpdate(getLastUpdateDate(ogrsAssessment,oasysAssessment ))
            .build();
    }
    private static LocalDate getLastUpdateDate( Optional<OGRSAssessment> ogrsAssessment, Optional<OASYSAssessment> oasysAssessment){
        var val1 = ogrsAssessment.map(OGRSAssessment::getLastUpdatedDate).orElse(null);
        var val2 = oasysAssessment.map(OASYSAssessment::getLastUpdatedDate).orElse(null);
        if (val1 != null && val2 != null) {
            return Stream.of(val1, val2).max(LocalDate::compareTo).orElse(null);
        }
        return Optional.ofNullable(val1).orElse(val2);
    }
    private static Integer getOGRSScore(Optional<OGRSAssessment> ogrsAssessment, Optional<OASYSAssessment> oasysAssessment) {
        LocalDate oasysAssessmentDate = oasysAssessment.map(OASYSAssessment::getAssessmentDate).orElse(null);
        Integer oasysOgrsScore2 = oasysAssessment.map(OASYSAssessment::getOGRSScore2).orElse(null);

        return ogrsAssessment.map(OGRS -> {
            var ogrs3Score2 = OGRS.getOGRS3Score2();
            if (oasysOgrsScore2 == null) {
                return ogrs3Score2;
            }else{
                if (ogrs3Score2 != null) {
                    if (OGRS.getAssessmentDate().isAfter(oasysAssessmentDate)) {
                        return ogrs3Score2;
                    }
                }
            }
            return null;
        }).orElse(oasysOgrsScore2);
    }
}
