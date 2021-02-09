package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.CourtAppearanceMinimal;

public class CourtAppearanceMinimalTransformer {

    public static CourtAppearanceMinimal courtAppearanceOf(uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance courtAppearance) {
        return CourtAppearanceMinimal.builder()
            .courtAppearanceId(courtAppearance.getCourtAppearanceId())
            .appearanceDate(courtAppearance.getAppearanceDate())
            .courtCode(courtAppearance.getCourt().getCode())
            .courtName(courtAppearance.getCourt().getCourtName())
            .appearanceType(CourtAppearanceBasicTransformer.appearanceTypeOf(courtAppearance.getAppearanceType()))
            .build();
    }
}
