package uk.gov.justice.digital.delius.jpa.standard.entity;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.util.EntityHelper.aKeyDate;

public class KeyDateTest {

    @Test
    public void isSentenceExpiryWhenKeyDateIsSED() {
        assertThat(KeyDate.isSentenceExpiryKeyDate("SED")).isTrue();
    }

    @Test
    public void isNotSentenceExpiryWhenKeyDateIsPOM() {
        assertThat(KeyDate.isSentenceExpiryKeyDate("POM1")).isFalse();
    }
    @Test
    public void isNotSentenceExpiryWhenKeyDateIsNULL() {
        assertThat(KeyDate.isSentenceExpiryKeyDate(null)).isFalse();
    }

    @Test
    @DisplayName("is licence expiry date when code is LED")
    void isLicenceExpiryDateWhenCodeIsLED() {
      assertThat(aKeyDate("SED", "Sentence expiry").isLicenceExpiryDate()).isFalse();
      assertThat(new KeyDate().isLicenceExpiryDate()).isFalse();
      assertThat(aKeyDate("LED", "Licence expiry").isLicenceExpiryDate()).isTrue();
    }

    @Test
    @DisplayName("is in the past when date is in the past")
    void isInThePastWhenDateIsInThePast() {
        assertThat(new KeyDate().isDateInPast()).isFalse();
        assertThat(aKeyDate("SED", "Sentence expiry", LocalDate.now()).isDateInPast()).isFalse();
        assertThat(aKeyDate("SED", "Sentence expiry", LocalDate.now().plusDays(1)).isDateInPast()).isFalse();
        assertThat(aKeyDate("SED", "Sentence expiry", LocalDate.now().minusDays(1)).isDateInPast()).isTrue();
    }
}
