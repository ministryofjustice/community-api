package uk.gov.justice.digital.delius.transformers;

import java.util.UUID;

public class ReferralTransformer {
    private ReferralTransformer() {
        // static helpers only
    }

    public static String prefixReferralStartNotesWithUrn(final String notes, final UUID referralId) {
        if (referralId == null) {
            return notes;
        }
        return transformReferralIdToUrn(referralId) + "\n" + notes;
    }

    public static String transformReferralIdToUrn(final UUID referralId) {
        if (referralId == null) {
            return null;
        }
        return "urn:hmpps:interventions-referral:" + referralId;
    }
}
