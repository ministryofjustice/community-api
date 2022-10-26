package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.transformers.ReferralTransformer.prefixReferralStartNotesWithUrn;
import static uk.gov.justice.digital.delius.transformers.ReferralTransformer.transformReferralIdToUrn;

class ReferralTransformerTest {
    @Test
    public void generatesAUrnFromAReferralId() {
        final var referralId = UUID.randomUUID();
        assertThat(transformReferralIdToUrn(referralId))
            .isEqualTo("urn:hmpps:interventions-referral:" + referralId);
    }

    @Test
    public void generatesNullURN_WhenReferralIdIsMissing() {
        assertThat(transformReferralIdToUrn(null))
            .isEqualTo(null);
    }

    @Test
    public void prefixesNotesWithReferralIdUrn() {
        final var referralId = UUID.randomUUID();
        assertThat(prefixReferralStartNotesWithUrn("some notes", referralId))
            .isEqualTo("urn:hmpps:interventions-referral:" + referralId + "\nsome notes");
    }

    @Test
    public void doesNotPrefixesNotesWithUrn_WhenReferralIdIsMissing() {
        assertThat(prefixReferralStartNotesWithUrn("some notes", null))
            .isEqualTo("some notes");
    }
}
