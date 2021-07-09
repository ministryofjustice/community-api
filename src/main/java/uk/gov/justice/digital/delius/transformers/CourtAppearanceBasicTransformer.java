package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.CourtAppearanceBasic;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.standard.entity.Court;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CourtAppearanceBasicTransformer {

    public static CourtAppearanceBasic courtAppearanceOf(CourtAppearance courtAppearance) {
        final var court = Optional.ofNullable(courtAppearance.getCourt());
        return CourtAppearanceBasic.builder()
            .courtAppearanceId(courtAppearance.getCourtAppearanceId())
            .appearanceDate(courtAppearance.getAppearanceDate())
            .courtCode(court.map(Court::getCode).orElse(null))
            .courtName(court.map(Court::getCourtName).orElse(null))
            .appearanceType(appearanceTypeOf(courtAppearance.getAppearanceType()))
            .crn(Optional.ofNullable(courtAppearance.getOffender()).map(Offender::getCrn).orElse(null))
            .build();
    }

    static KeyValue appearanceTypeOf(StandardReference establishmentType) {
        return Optional.ofNullable(establishmentType)
            .map(et -> KeyValue.builder()
            .code(et.getCodeValue())
            .description(et.getCodeDescription())
            .build())
            .orElse(null);
    }


    public static CourtAppearanceBasic latestOrSentencingCourtAppearanceOf(List<CourtAppearance> courtAppearances) {
        return courtAppearances
            .stream()
            .filter(CourtAppearance::isSentencing)
            .max(Comparator.comparing(CourtAppearance::getAppearanceDate))
            .map(CourtAppearanceBasicTransformer::courtAppearanceOf)
            .orElseGet(() -> courtAppearances
                // otherwise use the latest appearance
                .stream()
                .max(Comparator.comparing(CourtAppearance::getAppearanceDate))
                .map(CourtAppearanceBasicTransformer::courtAppearanceOf)
                .orElse(null)
            );
    }
}
