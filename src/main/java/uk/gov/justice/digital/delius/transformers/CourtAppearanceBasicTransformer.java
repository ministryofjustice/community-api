package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.CourtAppearanceBasic;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.util.Optional;

public class CourtAppearanceBasicTransformer {

    public static CourtAppearanceBasic courtAppearanceOf(uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance courtAppearance) {
        return CourtAppearanceBasic.builder()
            .courtAppearanceId(courtAppearance.getCourtAppearanceId())
            .appearanceDate(courtAppearance.getAppearanceDate())
            .courtCode(courtAppearance.getCourt().getCode())
            .appearanceType(appearanceTypeOf(courtAppearance.getAppearanceType()))
            .crn(courtAppearance.getOffender().getCrn())
            .build();
    }

    private static KeyValue appearanceTypeOf(StandardReference establishmentType) {
        return Optional.ofNullable(establishmentType).map(et -> KeyValue.builder()
                .code(et.getCodeValue())
                .description(et.getCodeDescription())
                .build())
                .orElse(null);
    }
}
